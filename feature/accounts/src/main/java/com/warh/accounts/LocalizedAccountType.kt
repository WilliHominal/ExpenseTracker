package com.warh.accounts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.warh.domain.models.AccountType

@Composable
fun AccountType.localized(): String = when (this) {
    AccountType.CASH   -> stringResource(R.string.account_type_cash)
    AccountType.BANK   -> stringResource(R.string.account_type_bank)
    AccountType.LOAN   -> stringResource(R.string.account_type_loan)
    AccountType.WALLET -> stringResource(R.string.account_type_wallet)
    AccountType.OTHER  -> stringResource(R.string.account_type_other)
}