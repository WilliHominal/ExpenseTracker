package com.warh.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.domain.models.Budget
import com.warh.domain.models.Category
import com.warh.domain.repositories.CategoryRepository
import com.warh.domain.use_cases.GetBudgetProgressForMonthUseCase
import com.warh.domain.use_cases.RemoveBudgetUseCase
import com.warh.domain.use_cases.UpsertBudgetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class BudgetsUiState(
    val year: Int = LocalDate.now().year,
    val month: Int = LocalDate.now().monthValue,
    val items: List<BudgetRow> = emptyList(),
    val categories: List<Category> = emptyList(),
    val showDialog: Boolean = false,
    val editing: Budget? = null,
)

data class BudgetRow(val categoryId: Long, val categoryName: String, val limitMinor: Long, val spentMinor: Long, val ratio: Float)

class BudgetsViewModel(
    private val progressForMonth: GetBudgetProgressForMonthUseCase,
    private val upsertBudget: UpsertBudgetUseCase,
    private val removeBudget: RemoveBudgetUseCase,
    private val categoryRepo: CategoryRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(BudgetsUiState())
    val ui: StateFlow<BudgetsUiState> = _ui

    init { refresh(); loadCategories() }

    private fun loadCategories() = viewModelScope.launch {
        _ui.update { it.copy(categories = categoryRepo.all()) }
    }

    private fun refresh() {
        val s = ui.value
        viewModelScope.launch {
            val p = progressForMonth(s.year, s.month)
            _ui.update { st ->
                st.copy(items = p.map { pr ->
                    BudgetRow(pr.category.id, pr.category.name, pr.limitMinor, pr.spentMinor, pr.ratio)
                })
            }
        }
    }

    fun openNew() { _ui.update { it.copy(showDialog = true, editing = null) } }
    fun openEdit(categoryId: Long, currentLimit: Long) { _ui.update { it.copy(showDialog = true, editing = Budget(categoryId, it.year, it.month, currentLimit)) } }
    fun closeDialog() { _ui.update { it.copy(showDialog = false, editing = null) } }

    fun save(categoryId: Long, limitMinor: Long) {
        val s = ui.value
        viewModelScope.launch {
            upsertBudget(Budget(categoryId, s.year, s.month, limitMinor))
            closeDialog(); refresh()
        }
    }

    fun remove(categoryId: Long) {
        val s = ui.value
        viewModelScope.launch { removeBudget(categoryId, s.year, s.month); refresh() }
    }
}