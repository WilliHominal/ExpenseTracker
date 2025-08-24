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
import java.time.format.DateTimeFormatter

private val YM: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

fun TransactionEntity.toDomain() = Transaction(
    id, accountId, TxType.valueOf(type), amountMinor, currency, date, categoryId, merchant, note
)

fun Transaction.toEntity() = TransactionEntity(
    id, accountId, type.name, amountMinor, currency, date, date.format(YM), categoryId, merchant, note
)

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    type = parseAccountType(type),
    currency = currency,
    balance = balance,
    initialBalance = initialBalance,
    iconIndex = iconIndex,
    iconColorArgb = iconColorArgb
)

fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    type = type.name,
    currency = currency,
    balance = balance,
    initialBalance = initialBalance,
    iconIndex = iconIndex,
    iconColorArgb = iconColorArgb
)

fun Budget.toEntity() = BudgetEntity(categoryId, year, month, limitMinor)

fun BudgetEntity.toDomain() = Budget(categoryId, year, month, limitMinor)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    iconIndex = iconIndex,
    iconColorArgb = iconColorArgb,
    type = parseTxType(type)
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    iconIndex = iconIndex,
    iconColorArgb = iconColorArgb,
    type = type.name
)

private fun parseAccountType(raw: String?): AccountType =
    runCatching { AccountType.valueOf(raw ?: "OTHER") }.getOrElse { AccountType.OTHER }

private fun parseTxType(raw: String?): TxType =
    runCatching { TxType.valueOf(raw ?: "EXPENSE") }.getOrElse { TxType.EXPENSE }