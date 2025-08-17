package com.warh.domain.use_cases

import com.warh.domain.models.Account
import com.warh.domain.repositories.AccountRepository

class GetAccountsUseCase(private val repo: AccountRepository) {
    suspend operator fun invoke(): List<Account> = repo.all()
}

class GetAccountUseCase(private val repo: AccountRepository) {
    suspend operator fun invoke(id: Long): Account? = repo.get(id)
}

class UpsertAccountUseCase(private val repo: AccountRepository) {
    suspend operator fun invoke(account: Account): Long = repo.upsert(account)
}

class DeleteAccountUseCase(private val repo: AccountRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}

class CanDeleteAccountUseCase(private val repo: AccountRepository) {
    suspend operator fun invoke(id: Long): Boolean = repo.txCountForAccount(id) == 0
}