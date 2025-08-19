package com.warh.accounts.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.domain.models.Transaction
import com.warh.domain.models.TransactionFilter
import com.warh.domain.models.TxType
import com.warh.domain.use_cases.GetAccountTransactionsUseCase
import com.warh.domain.use_cases.GetAccountUseCase
import com.warh.domain.use_cases.GetCategoriesUseCase
import com.warh.domain.use_cases.GetMonthlyCategorySpendUseCase
import com.warh.domain.use_cases.GetMonthlySumsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val getMonthlySums: GetMonthlySumsUseCase,
    private val getMonthlyCategorySpend: GetMonthlyCategorySpendUseCase
) : ViewModel() {

    companion object {
        private const val CURRENCY_DECIMALS = 2
    }

    private val _state = MutableStateFlow(AccountDetailUiState())
    val state: StateFlow<AccountDetailUiState> = _state

    private val zone = ZoneId.systemDefault()
    private fun today(): LocalDate = LocalDate.now(zone)

    private var loadJob: Job? = null

    fun load(accountId: Long, initial: PeriodFilter = PeriodFilter.Monthly) {
        _state.update {
            it.copy(isLoading = true, accountId = accountId, period = initial)
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val filter = buildFilter(initial)
            val ym = YearMonth.from(today())

            val (fromForSums, toForSums) = when (initial) {
                is PeriodFilter.Monthly -> {
                    val start = ym.minusMonths(5).atDay(1).atStartOfDay()
                    val end   = ym.plusMonths(1).atDay(1).atStartOfDay()
                    start to end
                }
                is PeriodFilter.Lifetime -> {
                    null to LocalDateTime.of(today(), LocalTime.MAX)
                }
            }

            val namesDef = async {
                io { runCatching { getCategories().associate { c -> c.id to c.name } }.getOrDefault(emptyMap()) }
            }
            val accNameDef = async {
                io { runCatching { getAccount(accountId)?.name }.getOrNull() ?: "Cuenta #$accountId" }
            }
            val txsDef = async {
                io { getAccountTransactions(accountId, filter) }
            }
            val monthlyDtoDef = async {
                io { getMonthlySums(fromForSums, toForSums, accountId) }
            }
            val byCatDtoDef = async {
                if (initial is PeriodFilter.Monthly)
                    io { getMonthlyCategorySpend(ym, accountId) }
                else
                    emptyList()
            }

            val names = namesDef.await()
            val accountName = accNameDef.await()
            val txs = txsDef.await()
            val monthlyDto = monthlyDtoDef.await()
            val byCatDto = byCatDtoDef.await()

            val monthly = monthlyDto.map { d ->
                MonthlyBucket(
                    month = YearMonth.parse(d.yearMonth),
                    incomeMajor = d.incomeMinor.toMajor(CURRENCY_DECIMALS),
                    expenseMajor = d.expenseMinor.toMajor(CURRENCY_DECIMALS)
                )
            }

            val totalIncomeMajor = monthlyDto.sumOf { it.incomeMinor }.toMajor(CURRENCY_DECIMALS)
            val totalExpenseMajor = monthlyDto.sumOf { it.expenseMinor }.toMajor(CURRENCY_DECIMALS)
            val balanceMajor = totalIncomeMajor.subtract(totalExpenseMajor)

            val thisMExp = monthly.lastOrNull()?.expenseMajor ?: BigDecimal.ZERO
            val prevMExp = monthly.dropLast(1).lastOrNull()?.expenseMajor ?: BigDecimal.ZERO
            val insightMoMDeltaPct =
                if (prevMExp > BigDecimal.ZERO)
                    thisMExp.subtract(prevMExp).multiply(BigDecimal(100)).divide(prevMExp, 2, RoundingMode.HALF_UP)
                else null

            val categoryDistribution =
                if (initial is PeriodFilter.Monthly && byCatDto.isNotEmpty()) {
                    byCatDto.map { d ->
                        CategoryDistribution(d.categoryId, d.spentMinor.toMajor(CURRENCY_DECIMALS))
                    }
                } else {
                    txs.filter { it.type == TxType.EXPENSE }
                        .groupBy { it.categoryId }
                        .map { (catId, list) -> CategoryDistribution(catId, list.sumOfMinor().toMajor(CURRENCY_DECIMALS)) }
                        .sortedByDescending { it.totalMajor }
                }

            val insightTopCategoryId =
                if (initial is PeriodFilter.Monthly && byCatDto.isNotEmpty()) byCatDto.first().categoryId
                else categoryDistribution.firstOrNull()?.categoryId

            _state.update { prev ->
                prev.copy(
                    isLoading = false,
                    totalIncomeMajor = totalIncomeMajor,
                    totalExpenseMajor = totalExpenseMajor,
                    balanceMajor = balanceMajor,
                    categoryDistribution = categoryDistribution,
                    monthly = monthly,
                    transactions = txs,
                    insightTopCategoryId = insightTopCategoryId,
                    insightMoMDeltaPct = insightMoMDeltaPct,
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

    private fun Iterable<Transaction>.sumOfMinor(): Long {
        var acc = 0L
        for (t in this) acc += t.amountMinor
        return acc
    }

    private fun Long.toMajor(decimals: Int): BigDecimal = BigDecimal.valueOf(this).movePointLeft(decimals)

    private suspend fun <T> io(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { block() }
}