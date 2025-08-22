package com.warh.categories.utils

import com.warh.domain.models.TxType
import com.warh.commons.R.drawable as CommonDrawables

object CategoriesIcons {
    fun iconsFor(type: TxType): List<Int> = when (type) {
        TxType.EXPENSE -> expenseIconIds
        TxType.INCOME  -> incomeIconIds
    }

    private val expenseIconIds = listOf(
        CommonDrawables.ic_categories_exp_food,
        CommonDrawables.ic_categories_exp_transport,
        CommonDrawables.ic_categories_exp_home,
        CommonDrawables.ic_categories_exp_shopping,
        CommonDrawables.ic_categories_exp_health,
        CommonDrawables.ic_categories_exp_education,
        CommonDrawables.ic_categories_exp_travel,
        CommonDrawables.ic_categories_exp_entertainment,
        CommonDrawables.ic_categories_exp_pets,
        CommonDrawables.ic_categories_exp_repairs,
        CommonDrawables.ic_categories_exp_utilities,
        CommonDrawables.ic_categories_exp_hobbies,
        CommonDrawables.ic_categories_exp_clothing,
        CommonDrawables.ic_categories_both_other,
    )

    private val incomeIconIds = listOf(
        CommonDrawables.ic_categories_inc_salary,
        CommonDrawables.ic_categories_inc_investments,
        CommonDrawables.ic_categories_inc_bonuses,
        CommonDrawables.ic_categories_inc_gifts,
        CommonDrawables.ic_categories_inc_prizes,
        CommonDrawables.ic_categories_inc_refunds,
        CommonDrawables.ic_categories_inc_rentals,
        CommonDrawables.ic_categories_inc_sales,
        CommonDrawables.ic_categories_inc_savings,
        CommonDrawables.ic_categories_both_other,
    )
}