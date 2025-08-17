package com.warh.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.Category
import com.warh.domain.use_cases.CanDeleteCategoryUseCase
import com.warh.domain.use_cases.DeleteCategoryUseCase
import com.warh.domain.use_cases.GetCategoriesUseCase
import com.warh.domain.use_cases.UpsertCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoriesUiState(
    val items: List<Category> = emptyList(),
    val draft: CategoryDraft? = null,
    val error: String? = null
)

data class CategoryDraft(
    val id: Long? = null,
    val name: String = "",
    val colorArgb: Long = 0xFF9E9E9E
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
        _ui.update { it.copy(items = getCategories(), draft = null, error = null) }
    }

    fun startAdd() = _ui.update { it.copy(draft = CategoryDraft()) }
    fun startEdit(c: Category) = _ui.update {
        it.copy(draft = CategoryDraft(c.id, c.name, c.colorArgb))
    }
    fun cancel() = _ui.update { it.copy(draft = null) }

    fun onName(v: String)      = updateDraft { it.copy(name = v) }
    fun onColor(argb: Long)    = updateDraft { it.copy(colorArgb = argb) }

    private fun updateDraft(block: (CategoryDraft) -> CategoryDraft) =
        _ui.update { s -> s.copy(draft = s.draft?.let(block)) }

    fun save() {
        val d = _ui.value.draft ?: return
        if (d.name.isBlank()) { _ui.update { it.copy(error = strings[R.string.categories_error_name_required]) }; return }
        viewModelScope.launch {
            upsert(Category(id = d.id ?: 0L, name = d.name.trim(), colorArgb = d.colorArgb))
            refresh()
        }
    }

    fun remove(id: Long, onBlocked: (String) -> Unit) {
        viewModelScope.launch {
            if (!canDelete(id)) {
                onBlocked(strings[R.string.categories_error_delete_blocked])
                return@launch
            }
            delete(id)
            refresh()
        }
    }
}