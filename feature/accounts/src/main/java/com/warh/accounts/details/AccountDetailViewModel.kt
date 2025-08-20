package com.warh.accounts.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.accounts.R
import com.warh.commons.Strings
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
import java.util.Currency
import java.util.Locale

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
    val insightMoMDeltaPct: BigDecimal? = null,
    val accountCurrencyCode: String = Currency.getInstance(Locale.getDefault()).currencyCode
)

class AccountDetailViewModel(
    private val getAccountTransactions: GetAccountTransactionsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val getAccount: GetAccountUseCase,
    private val getMonthlySums: GetMonthlySumsUseCase,
    private val getMonthlyCategorySpend: GetMonthlyCategorySpendUseCase,
    private val strings: Strings
) : ViewModel() {

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
                is PeriodFilter.Lifetime -> null to LocalDateTime.of(today(), LocalTime.MAX)
            }

            val accountDef = async { io { getAccount(accountId) } }
            val namesDef   = async { io { runCatching { getCategories().associate { it.id to it.name } }.getOrDefault(emptyMap()) } }
            val txsDef     = async { io { getAccountTransactions(accountId, filter) } }
            val monthlyDef = async { io { getMonthlySums(fromForSums, toForSums, accountId) } }
            val byCatDef   = async {
                if (initial is PeriodFilter.Monthly) io { getMonthlyCategorySpend(ym, accountId) } else emptyList()
            }

            val account     = accountDef.await()
            val currencyCode = account?.currency
                ?: Currency.getInstance( Locale.getDefault()).currencyCode
            val decimals = runCatching { Currency.getInstance(currencyCode).defaultFractionDigits }
                .getOrDefault(2).coerceAtLeast(0)

            val names      = namesDef.await()
            val txs        = txsDef.await()
            val monthlyDto = monthlyDef.await()
            val byCatDto   = byCatDef.await()

            val monthly = monthlyDto.map { d ->
                MonthlyBucket(
                    month = YearMonth.parse(d.yearMonth),
                    incomeMajor  = d.incomeMinor.toMajor(decimals),
                    expenseMajor = d.expenseMinor.toMajor(decimals)
                )
            }

            val totalIncomeMajor  = monthlyDto.sumOf { it.incomeMinor }.toMajor(decimals)
            val totalExpenseMajor = monthlyDto.sumOf { it.expenseMinor }.toMajor(decimals)
            val balanceMajor      = totalIncomeMajor.subtract(totalExpenseMajor)

            val thisMExp = monthly.lastOrNull()?.expenseMajor ?: BigDecimal.ZERO
            val prevMExp = monthly.dropLast(1).lastOrNull()?.expenseMajor ?: BigDecimal.ZERO
            val insightMoMDeltaPct =
                if (prevMExp > BigDecimal.ZERO)
                    thisMExp.subtract(prevMExp).multiply(BigDecimal(100)).divide(prevMExp, 2, RoundingMode.HALF_UP)
                else null

            val categoryDistribution =
                if (initial is PeriodFilter.Monthly && byCatDto.isNotEmpty()) {
                    byCatDto.map { CategoryDistribution(it.categoryId, it.spentMinor.toMajor(decimals)) }
                } else {
                    txs.filter { it.type == TxType.EXPENSE }
                        .groupBy { it.categoryId }
                        .map { (catId, list) -> CategoryDistribution(catId, list.sumOfMinor().toMajor(decimals)) }
                        .sortedByDescending { it.totalMajor }
                }

            val insightTopCategoryId =
                if (initial is PeriodFilter.Monthly && byCatDto.isNotEmpty()) byCatDto.first().categoryId
                else categoryDistribution.firstOrNull()?.categoryId

            _state.update {
                it.copy(
                    isLoading = false,
                    accountName = account?.name ?: strings.format(R.string.account_detail_default_name, accountId.toString()),
                    accountCurrencyCode = currencyCode,
                    totalIncomeMajor = totalIncomeMajor,
                    totalExpenseMajor = totalExpenseMajor,
                    balanceMajor = balanceMajor,
                    categoryDistribution = categoryDistribution,
                    monthly = monthly,
                    transactions = txs,
                    insightTopCategoryId = insightTopCategoryId,
                    insightMoMDeltaPct = insightMoMDeltaPct,
                    categoryNames = names
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