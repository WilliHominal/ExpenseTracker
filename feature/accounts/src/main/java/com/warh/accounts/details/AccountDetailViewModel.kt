package com.warh.accounts.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import com.warh.domain.models.TxType
import com.warh.domain.use_cases.GetAccountTransactionsUseCase
import com.warh.domain.use_cases.GetAccountUseCase
import com.warh.domain.use_cases.GetCategoriesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

sealed interface PeriodFilter {
    data object Monthly : PeriodFilter
    data object Lifetime : PeriodFilter
}

data class CategoryDistribution(
    val categoryId: Long?,
    val totalMajor: BigDecimal
)

data class MonthlyBucket(
    val month: YearMonth,
    val incomeMajor: BigDecimal,
    val expenseMajor: BigDecimal
)

data class AccountDetailUiState(
    val isLoading: Boolean = true,
    val accountId: Long = 0L,
    val accountName: String = "",
    val categoryNames: Map<Long, String> = emptyMap(),
    val period: PeriodFilter = PeriodFilter.Monthly,
    val totalIncomeMajor: BigDecimal = BigDecimal.ZERO,
    val totalExpenseMajor: BigDecimal = BigDecimal.ZERO,
    val balanceMajor: BigDecimal = BigDecimal.ZERO,
    val categoryDistribution: List<CategoryDistribution> = emptyList(),
    val monthly: List<MonthlyBucket> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val insightTopCategoryId: Long? = null,
    val insightMoMDeltaPct: BigDecimal? = null
)

class AccountDetailViewModel(
    private val getAccountTransactions: GetAccountTransactionsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val getAccount: GetAccountUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AccountDetailUiState())
    val state: StateFlow<AccountDetailUiState> = _state

    private val zone = ZoneId.systemDefault()
    private fun today(): LocalDate = LocalDate.now(zone)

    private val CURRENCY_DECIMALS = 2

    fun load(accountId: Long, initial: PeriodFilter = PeriodFilter.Monthly) {
        _state.update {
            it.copy(
                isLoading = true,
                accountId = accountId,
                period = initial,
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val names: Map<Long, String> = runCatching {
                getCategories.invoke().associate { it.id to it.name }
            }.getOrNull() ?: emptyMap()

            val accountName = runCatching {
                getAccount.invoke(accountId)
            }.getOrNull()?.name ?: "Cuenta #${accountId}"

            val filter = buildFilter(initial)
            val txs = getAccountTransactions(accountId, filter)

            val computed = compute(txs)

            _state.update { prev ->
                prev.copy(
                    isLoading = false,
                    totalIncomeMajor = computed.totalIncomeMajor,
                    totalExpenseMajor = computed.totalExpenseMajor,
                    balanceMajor = computed.balanceMajor,
                    categoryDistribution = computed.categoryDistribution,
                    monthly = computed.monthly,
                    transactions = txs,
                    insightTopCategoryId = computed.topCategoryId,
                    insightMoMDeltaPct = computed.momDeltaPct,
                    categoryNames = names,
                    accountName = accountName
                )
            }
        }
    }

    fun onPeriodChange(newPeriod: PeriodFilter) {
        load(state.value.accountId, newPeriod)
    }

    private fun buildFilter(period: PeriodFilter): TransactionFilter = when (period) {
        PeriodFilter.Monthly -> {
            val ym = YearMonth.from(today())
            TransactionFilter(
                from = ym.atDay(1).atStartOfDay(),
                to = ym.atEndOfMonth().plusDays(1).atStartOfDay(),
                text = null
            )
        }
        PeriodFilter.Lifetime -> {
            TransactionFilter(
                from = null,
                to = LocalDateTime.of(today(), LocalTime.MAX),
                text = null
            )
        }
    }

    private data class ComputeResult(
        val totalIncomeMajor: BigDecimal,
        val totalExpenseMajor: BigDecimal,
        val balanceMajor: BigDecimal,
        val categoryDistribution: List<CategoryDistribution>,
        val monthly: List<MonthlyBucket>,
        val topCategoryId: Long?,
        val momDeltaPct: BigDecimal?
    )

    private fun compute(periodTxs: List<Transaction>): ComputeResult {
        val totalIncomeMinor = periodTxs.filter { it.type == TxType.INCOME }.sumOfMinor()
        val totalExpenseMinor = periodTxs.filter { it.type == TxType.EXPENSE }.sumOfMinor()
        val balanceMinor = totalIncomeMinor - totalExpenseMinor

        val byCategoryMinor: List<Pair<Long?, Long>> = periodTxs
            .filter { it.type == TxType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (catId, txs) -> catId to txs.sumOfMinor() }
            .sortedByDescending { it.second }

        val last6 = generateSequence(YearMonth.from(today())) { it.minusMonths(1) }
            .take(6).toList().reversed()

        val monthlyMinor = last6.map { ym ->
            val inMonth = periodTxs.filter { YearMonth.from(it.date.toLocalDate()) == ym }
            val incomeM = inMonth.filter { it.type == TxType.INCOME }.sumOfMinor()
            val expenseM = inMonth.filter { it.type == TxType.EXPENSE }.sumOfMinor()
            Triple(ym, incomeM, expenseM)
        }

        val thisMExp = monthlyMinor.lastOrNull()?.third ?: 0L
        val prevMExp = monthlyMinor.dropLast(1).lastOrNull()?.third ?: 0L
        val momDeltaPct: BigDecimal? =
            if (prevMExp > 0L) {
                val diff = thisMExp - prevMExp
                BigDecimal(diff).multiply(BigDecimal(100))
                    .divide(BigDecimal(prevMExp), 2, RoundingMode.HALF_UP)
            } else null

        return ComputeResult(
            totalIncomeMajor = totalIncomeMinor.toMajor(CURRENCY_DECIMALS),
            totalExpenseMajor = totalExpenseMinor.toMajor(CURRENCY_DECIMALS),
            balanceMajor = balanceMinor.toMajor(CURRENCY_DECIMALS),
            categoryDistribution = byCategoryMinor.map { (catId, sumMinor) ->
                CategoryDistribution(catId, sumMinor.toMajor(CURRENCY_DECIMALS))
            },
            monthly = monthlyMinor.map { (ym, inc, exp) ->
                MonthlyBucket(
                    month = ym,
                    incomeMajor = inc.toMajor(CURRENCY_DECIMALS),
                    expenseMajor = exp.toMajor(CURRENCY_DECIMALS)
                )
            },
            topCategoryId = byCategoryMinor.firstOrNull()?.first,
            momDeltaPct = momDeltaPct
        )
    }

    private fun Iterable<Transaction>.sumOfMinor(): Long {
        var acc = 0L
        for (t in this) acc += t.amountMinor
        return acc
    }

    private fun Long.toMajor(decimals: Int): BigDecimal =
        BigDecimal.valueOf(this).movePointLeft(decimals)

    private operator fun BigDecimal.plus(other: BigDecimal) = this.add(other)
    private operator fun BigDecimal.minus(other: BigDecimal) = this.subtract(other)
}