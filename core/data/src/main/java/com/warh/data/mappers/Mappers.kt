package com.warh.data.mappers

import com.warh.data.entities.AccountEntity
import com.warh.data.entities.BudgetEntity
import com.warh.data.entities.CategoryEntity
import com.warh.data.entities.TransactionEntity
import com.warh.domain.models.Account
import com.warh.domain.models.AccountType
import com.warh.domain.models.Budget
import com.warh.domain.models.Category
import com.warh.domain.models.Transaction
import com.warh.domain.models.TxType

fun TransactionEntity.toDomain() = Transaction(
    id, accountId, TxType.valueOf(type), amountMinor, currency, date, categoryId, merchant, note
)

fun Transaction.toEntity() = TransactionEntity(
    id, accountId, type.name, amountMinor, currency, date, categoryId, merchant, note
)

fun AccountEntity.toDomain() = Account(id, name, AccountType.valueOf(type), currency, balanceMinor)

fun Account.toEntity() = AccountEntity(
    id, name, type.name, currency, balanceMinor
)

fun Budget.toEntity() = BudgetEntity(categoryId, year, month, limitMinor)

fun BudgetEntity.toDomain() = Budget(categoryId, year, month, limitMinor)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    colorArgb = colorArgb
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    colorArgb = colorArgb
)