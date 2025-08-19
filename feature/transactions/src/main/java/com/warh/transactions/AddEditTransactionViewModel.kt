package com.warh.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.Account
import com.warh.domain.models.Category
import com.warh.domain.models.Transaction
import com.warh.domain.models.TxType
import com.warh.domain.use_cases.AddTransactionUseCase
import com.warh.domain.use_cases.GetAccountsUseCase
import com.warh.domain.use_cases.GetCategoriesUseCase
import com.warh.domain.use_cases.GetMerchantSuggestionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Currency
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

data class TxEditorUiState(
    val amountText: String = "",
    val type: TxType = TxType.EXPENSE,
    val accountId: Long? = null,
    val categoryId: Long? = null,
    val merchant: String = "",
    val note: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val merchantSuggestions: List<String> = emptyList(),
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AddEditTransactionViewModel(
    private val addTx: AddTransactionUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val getMerchantSuggestions: GetMerchantSuggestionsUseCase,
    private val strings: Strings,
) : ViewModel() {

    private val _ui = MutableStateFlow(TxEditorUiState())
    val ui: StateFlow<TxEditorUiState> = _ui

    private val merchantQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            val accounts = runCatching { io { getAccounts() } }.getOrDefault(emptyList())
            val categories = runCatching { io { getCategories() } }.getOrDefault(emptyList())
            _ui.update {
                it.copy(
                    accounts = accounts,
                    categories = categories,
                    accountId = accounts.firstOrNull()?.id
                )
            }
        }

        viewModelScope.launch {
            merchantQuery
                .map { it.trim() }
                .debounce(250)
                .distinctUntilChanged()
                .flatMapLatest { s ->
                    if (s.length >= 2) flow {
                        val items = runCatching { io { getMerchantSuggestions(s) } }
                            .getOrElse { emptyList() }
                        emit(items)
                    } else {
                        flowOf(emptyList())
                    }
                }
                .collect { items ->
                    _ui.update { it.copy(merchantSuggestions = items) }
                }
        }
    }

    fun onAmountChange(v: String) = _ui.update { st ->
        val code = st.accounts.firstOrNull { it.id == st.accountId }?.currency
            ?: Currency.getInstance(Locale.getDefault()).currencyCode
        val decimals = runCatching { Currency.getInstance(code).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)

        st.copy(amountText = normalizeAmountInput(v, decimals))
    }
    fun onTypeChange(v: TxType) = _ui.update { it.copy(type = v) }
    fun onAccountChange(id: Long) = _ui.update { it.copy(accountId = id) }
    fun onCategoryChange(id: Long?) = _ui.update { it.copy(categoryId = id) }
    fun onNoteChange(v: String) = _ui.update { it.copy(note = v) }
    fun onDateChange(v: LocalDateTime) = _ui.update { it.copy(date = v) }

    fun onMerchantChange(v: String) {
        _ui.update { it.copy(merchant = v) }
        merchantQuery.value = v
    }

    fun onMerchantPick(s: String) {
        _ui.update { it.copy(merchant = s, merchantSuggestions = emptyList()) }
        merchantQuery.value = s
    }

    fun save(onSaved: () -> Unit) {
        val s = ui.value

        viewModelScope.launch {
            _ui.update { it.copy(isSaving = true, error = null) }

            val accountCurrency = ui.value.accounts
                .firstOrNull { it.id == s.accountId }
                ?.currency ?: Currency.getInstance(Locale.getDefault()).currencyCode

            val decimals = runCatching { Currency.getInstance(accountCurrency).defaultFractionDigits }
                .getOrDefault(2).coerceAtLeast(0)

            val amountMinor = parseAmountMinor(s.amountText, decimals)
            if (amountMinor <= 0 || s.accountId == null) {
                _ui.update { it.copy(error = strings[R.string.add_transaction_error_account_or_amount_invalid]) }
                return@launch
            }

            val tx = Transaction(
                id = System.currentTimeMillis(),
                accountId = s.accountId,
                type = s.type,
                amountMinor = amountMinor,
                currency = accountCurrency,
                date = s.date,
                categoryId = s.categoryId,
                merchant = s.merchant.ifBlank { null },
                note = s.note.ifBlank { null },
            )

            val result = runCatching { io { addTx(tx) } }
            _ui.update { it.copy(isSaving = false) }

            result.onSuccess { onSaved() }
                .onFailure { e ->
                    _ui.update {
                        it.copy(error = e.message ?: strings[R.string.add_transaction_error_default])
                    }
                }
        }
    }

    // --- Helpers ---
    private fun normalizeAmountInput(raw: String, decimals: Int): String {
        val filtered = raw.filter { it.isDigit() || it == '.' || it == ',' }
        var s = filtered.replace(',', '.')

        if (decimals == 0) {
            return s.takeWhile { it.isDigit() }.ifEmpty { "0" }
        }

        val firstDot = s.indexOf('.')
        if (firstDot >= 0) {
            val intPart = s.substring(0, firstDot).ifEmpty { "0" }
            val fracPart = s.substring(firstDot + 1).take(decimals)
            s = if (fracPart.isEmpty()) "$intPart." else "$intPart.$fracPart"
        }
        return s
    }

    private fun parseAmountMinor(input: String, decimals: Int): Long {
        val norm = input.replace(',', '.')
        val v = norm.toDoubleOrNull() ?: return 0
        val factor = 10.0.pow(decimals.toDouble())
        return (v * factor).roundToLong()
    }

    private suspend fun <T> io(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { block() }
}