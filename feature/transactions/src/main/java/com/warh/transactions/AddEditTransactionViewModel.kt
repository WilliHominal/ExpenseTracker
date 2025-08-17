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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Currency
import java.util.Locale
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

class AddEditTransactionViewModel(
    private val addTx: AddTransactionUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val getMerchantSuggestions: GetMerchantSuggestionsUseCase,
    private val strings: Strings,
) : ViewModel() {

    private val _ui = MutableStateFlow(TxEditorUiState())
    val ui: StateFlow<TxEditorUiState> = _ui

    //private var autoCatRules: List<Pair<Regex, Long>> = emptyList()

    init {
        viewModelScope.launch {
            val accs = getAccounts()
            val cats = getCategories()
            _ui.update { it.copy(accounts = accs, categories = cats, accountId = accs.firstOrNull()?.id) }
            //buildAutoCatRules(cats)
        }
    }

    /*private fun buildAutoCatRules(cats: List<Category>) {
        val byName = cats.associateBy { it.name.trim().lowercase() }
        fun id(name: String) = byName[name.trim().lowercase()]?.id
        autoCatRules = listOf(
            Regex("\\b(uber|cabify|didi)\\b", RegexOption.IGNORE_CASE) to id("Transporte"),
            Regex("\\b(mc|kfc|burger|subway|coto|carrefour|jumbo|disco)\\b", RegexOption.IGNORE_CASE) to id("Comida"),
            Regex("\\b(movistar|claro|personal)\\b", RegexOption.IGNORE_CASE) to id("Servicios"),
        ).mapNotNull { (rx, maybe) -> maybe?.let { rx to it } }

        ui.value.merchant.takeIf { it.isNotBlank() }?.let { tryAutoCategorize(it) }
    }

    private fun tryAutoCategorize(merchant: String) {
        val match = autoCatRules.firstOrNull { it.first.containsMatchIn(merchant) }
        if (match != null) {
            val (_, catId) = match
            _ui.update { it.copy(categoryId = catId) }
        }
    }*/

    fun onAmountChange(v: String) = _ui.update { it.copy(amountText = normalizeAmountInput(v)) }
    fun onTypeChange(v: TxType) = _ui.update { it.copy(type = v) }
    fun onAccountChange(id: Long) = _ui.update { it.copy(accountId = id) }
    fun onCategoryChange(id: Long?) = _ui.update { it.copy(categoryId = id) }
    fun onNoteChange(v: String) = _ui.update { it.copy(note = v) }
    fun onDateChange(v: LocalDateTime) = _ui.update { it.copy(date = v) }

    fun onMerchantChange(v: String) {
        _ui.update { it.copy(merchant = v) }

        viewModelScope.launch {
            val s = v.trim()
            val items = if (s.length >= 2) getMerchantSuggestions(s) else emptyList()
            _ui.update { it.copy(merchantSuggestions = items) }
        }

        //tryAutoCategorize(v)
    }

    fun onMerchantPick(s: String) {
        _ui.update { it.copy(merchant = s, merchantSuggestions = emptyList()) }
        onMerchantChange(s)
    }

    fun save(onSaved: () -> Unit) {
        val s = ui.value
        val amountMinor = parseAmountMinor(s.amountText)
        if (amountMinor <= 0 || s.accountId == null) {
            _ui.update { it.copy(error = strings[R.string.add_transaction_error_account_or_amount_invalid]) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(isSaving = true, error = null) }
            val tx = Transaction(
                id = System.currentTimeMillis(),
                accountId = s.accountId,
                type = s.type,
                amountMinor = amountMinor,
                currency = Currency.getInstance(Locale.getDefault()).currencyCode, //TODO: Agregar currency a la UI
                date = s.date,
                categoryId = s.categoryId,
                merchant = s.merchant.ifBlank { null },
                note = s.note.ifBlank { null },
            )
            runCatching { addTx(tx) }
                .onSuccess { onSaved() }
                .onFailure { e -> _ui.update { it.copy(isSaving = false, error = e.message ?: strings[R.string.add_transaction_error_default]) } }
        }
    }

    // --- Helpers ---
    private fun normalizeAmountInput(raw: String): String {
        val filtered = raw.filter { it.isDigit() || it == '.' || it == ',' }
        var s = filtered.replace(',', '.')
        val firstDot = s.indexOf('.')
        if (firstDot >= 0) {
            val intPart = s.substring(0, firstDot).ifEmpty { "0" }
            val fracPart = s.substring(firstDot + 1).take(2)
            s = if (fracPart.isEmpty()) "$intPart." else "$intPart.$fracPart"
        }
        return s
    }

    private fun parseAmountMinor(input: String): Long {
        val norm = input.replace(',', '.')
        val v = norm.toDoubleOrNull() ?: return 0
        return (v * 100.0).roundToLong()
    }
}