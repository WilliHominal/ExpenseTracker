package com.warh.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.Account
import com.warh.domain.models.Category
import com.warh.domain.models.Transaction
import com.warh.domain.models.TxType
import com.warh.domain.use_cases.GetCategoriesUseCase
import com.warh.domain.use_cases.GetMerchantSuggestionsUseCase
import com.warh.domain.use_cases.GetTransactionUseCase
import com.warh.domain.use_cases.ObserveAccountsUseCase
import com.warh.domain.use_cases.UpsertTransactionUseCase
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
    val isEditing: Boolean = false,
    val editingId: Long? = null,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AddEditTransactionViewModel(
    private val editingId: Long?,
    private val getTransaction: GetTransactionUseCase,
    private val upsertTx: UpsertTransactionUseCase,
    private val observeAccounts: ObserveAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val getMerchantSuggestions: GetMerchantSuggestionsUseCase,
    private val strings: Strings,
) : ViewModel() {

    private val _ui = MutableStateFlow(TxEditorUiState())
    val ui: StateFlow<TxEditorUiState> = _ui

    private val merchantQuery = MutableStateFlow("")
    private var allCategories: List<Category> = emptyList()

    private var editingAmountMinor: Long? = null
    private var appliedEditingPrefill = false

    init {
        viewModelScope.launch {
            observeAccounts().collect { accounts ->
                _ui.update { st ->
                    val newSelected =
                        st.accountId?.takeIf { sel -> accounts.any { it.id == sel } }
                            ?: accounts.firstOrNull()?.id
                    st.copy(accounts = accounts, accountId = newSelected)
                }

                if (!appliedEditingPrefill && _ui.value.isEditing && editingAmountMinor != null) {
                    val digits = digitsForSelectedAccount()
                    _ui.update { it.copy(amountText = formatForInput(editingAmountMinor!!, digits)) }
                    appliedEditingPrefill = true
                }
            }
        }

        viewModelScope.launch {
            val categories = runCatching { io { getCategories() } }.getOrDefault(emptyList())
            allCategories = categories
            _ui.update { st ->
                val filtered = allCategories.filter { it.type == st.type }
                val newCatId = filtered.firstOrNull { it.id == st.categoryId }?.id
                st.copy(categories = filtered, categoryId = newCatId)
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
                    } else flowOf(emptyList())
                }
                .collect { items -> _ui.update { it.copy(merchantSuggestions = items) } }
        }

        if (editingId != null) {
            viewModelScope.launch {
                val tx = runCatching { io { getTransaction(editingId) } }.getOrNull() ?: return@launch
                editingAmountMinor = tx.amountMinor
                val digits = digitsForSelectedAccount(accountId = tx.accountId)

                _ui.update {
                    it.copy(
                        isEditing  = true,
                        editingId  = tx.id,
                        amountText = formatForInput(tx.amountMinor, digits),
                        type       = tx.type,
                        accountId  = tx.accountId,
                        categoryId = tx.categoryId,
                        merchant   = tx.merchant.orEmpty(),
                        note       = tx.note.orEmpty(),
                        date       = tx.date
                    )
                }

                _ui.update { st ->
                    val filtered = allCategories.filter { it.type == st.type }
                    st.copy(categories = filtered)
                }
                appliedEditingPrefill = true
            }
        }
    }

    fun onAmountChange(v: String) = _ui.update { st ->
        val decimals = digitsForSelectedAccount()
        st.copy(amountText = normalizeAmountInput(v, decimals))
    }
    fun onTypeChange(v: TxType) = _ui.update { st ->
        val filtered = allCategories.filter { it.type == v }
        val keepSelected = filtered.any { it.id == st.categoryId }
        st.copy(
            type = v,
            categories = filtered,
            categoryId = if (keepSelected) st.categoryId else null
        )
    }
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

            val decimals = digitsForSelectedAccount()
            val amountMinor = parseAmountMinor(s.amountText, decimals)

            if (amountMinor <= 0 || s.accountId == null) {
                _ui.update { it.copy(error = strings[R.string.add_transaction_error_account_or_amount_invalid]) }
                _ui.update { it.copy(isSaving = false) }
                return@launch
            }

            val tx = Transaction(
                id          = s.editingId ?: System.currentTimeMillis(),
                accountId   = s.accountId,
                type        = s.type,
                amountMinor = amountMinor,
                date        = s.date,
                categoryId  = s.categoryId,
                merchant    = s.merchant.ifBlank { null },
                note        = s.note.ifBlank { null },
            )

            val result = runCatching { io { upsertTx(tx) } }
            _ui.update { it.copy(isSaving = false) }

            result.onSuccess { onSaved() }
                .onFailure { e ->
                    _ui.update { it.copy(error = e.message ?: strings[R.string.add_transaction_error_default]) }
                }
        }
    }

    // --- Helpers ---
    private fun digitsForSelectedAccount(accountId: Long? = _ui.value.accountId): Int {
        val code = _ui.value.accounts.firstOrNull { it.id == accountId }?.currency
            ?: Currency.getInstance(Locale.getDefault()).currencyCode
        return runCatching { Currency.getInstance(code).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)
    }

    private fun formatForInput(minor: Long, digits: Int): String {
        if (digits <= 0) return minor.toString()
        val abs = kotlin.math.abs(minor)
        val sign = if (minor < 0) "-" else ""
        val pow = 10.0.pow(digits).toLong().coerceAtLeast(1L)
        val intPart = abs / pow
        val fracPart = (abs % pow).toString().padStart(digits, '0')
        return "$sign$intPart.$fracPart"
    }

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