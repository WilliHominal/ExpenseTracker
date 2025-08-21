package com.warh.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.Category
import com.warh.domain.models.TxType
import com.warh.domain.use_cases.CanDeleteCategoryUseCase
import com.warh.domain.use_cases.DeleteCategoryUseCase
import com.warh.domain.use_cases.GetCategoriesUseCase
import com.warh.domain.use_cases.UpsertCategoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CategoriesUiState(
    val items: List<Category> = emptyList(),
    val draft: CategoryDraft? = null,
    val error: String? = null
)

data class CategoryDraft(
    val id: Long? = null,
    val name: String = "",
    val iconIndex: Int = 1,
    val iconColorArgb: Long? = null,
    val type: TxType = TxType.EXPENSE
)

class CategoriesViewModel(
    private val getCategories: GetCategoriesUseCase,
    private val upsert: UpsertCategoryUseCase,
    private val delete: DeleteCategoryUseCase,
    private val canDelete: CanDeleteCategoryUseCase,
    private val strings: Strings,
) : ViewModel() {

    private val _ui = MutableStateFlow(CategoriesUiState())
    val ui: StateFlow<CategoriesUiState> = _ui

    init { refresh() }

    private fun refresh() = viewModelScope.launch {
        val items = runCatching { io { getCategories() } }
            .getOrDefault(emptyList())

        _ui.update { it.copy(items = items, draft = null, error = null) }
    }

    fun startAdd() = _ui.update { it.copy(draft = CategoryDraft()) }

    fun startEdit(c: Category) = _ui.update {
        it.copy(draft = CategoryDraft(
            id = c.id,
            name = c.name,
            iconIndex = c.iconIndex,
            iconColorArgb = c.iconColorArgb,
            type = c.type
        ))
    }

    fun cancel() = _ui.update { it.copy(draft = null) }

    fun onName(v: String)           = updateDraft { it.copy(name = v) }
    fun onIconIndex(v: Int)         = updateDraft { it.copy(iconIndex = v) }
    fun onIconColor(v: Long?)       = updateDraft { it.copy(iconColorArgb = v) }
    fun onType(v: TxType)           = updateDraft { it.copy(type = v) }

    private fun updateDraft(block: (CategoryDraft) -> CategoryDraft) =
        _ui.update { s -> s.copy(draft = s.draft?.let(block)) }

    fun save() {
        val d = _ui.value.draft ?: return
        if (d.name.isBlank()) {
            _ui.update { it.copy(error = strings[R.string.categories_error_name_required]) }
            return
        }

        viewModelScope.launch {
            runCatching {
                io {
                    upsert(
                        Category(
                            id = d.id ?: 0L,
                            name = d.name.trim(),
                            iconIndex = d.iconIndex,
                            iconColorArgb = d.iconColorArgb,
                            type = d.type
                        )
                    )
                }
            }.onFailure { e ->
                _ui.update { it.copy(error = e.message ?: strings[R.string.categories_error_name_required]) }
            }
            refresh()
        }
    }

    fun remove(id: Long, onBlocked: (String) -> Unit) {
        viewModelScope.launch {
            val allowed = runCatching { io { canDelete(id) } }
                .getOrDefault(false)

            if (!allowed) {
                onBlocked(strings[R.string.categories_error_delete_blocked])
                return@launch
            }

            runCatching { io { delete(id) } }

            refresh()
        }
    }

    private suspend fun <T> io(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { block() }
}