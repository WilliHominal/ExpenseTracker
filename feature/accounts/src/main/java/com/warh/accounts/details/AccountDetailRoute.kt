package com.warh.accounts.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.warh.accounts.R
import com.warh.commons.CurrencyLabel
import com.warh.commons.NumberUtils
import com.warh.commons.TopBarDefault
import com.warh.domain.models.Transaction
import com.warh.domain.models.TxType
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Currency
import java.util.Locale
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailRoute(
    accountId: Long,
    onBack: () -> Unit,
    vm: AccountDetailViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(accountId) { vm.load(accountId) }

    Scaffold(
        topBar = {
            TopBarDefault(
                title = state.accountName,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.account_detail_cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.toggleListOnly() }) {
                        Icon(
                            imageVector = if (state.listOnly) Icons.Default.KeyboardArrowUp
                                          else Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.account_detail_cd_jump_to_list)
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            AccountDetailScreen(
                state = state,
                onPeriodChange = vm::onPeriodChange,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun AccountDetailScreen(
    state: AccountDetailUiState,
    onPeriodChange: (PeriodFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyCode = remember(state.accountCurrencyCode, Locale.getDefault()) {
        runCatching { Currency.getInstance(state.accountCurrencyCode).currencyCode }
            .getOrElse { Currency.getInstance(Locale.getDefault()).currencyCode }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(
            visible = !state.listOnly,
            enter = fadeIn(animationSpec = tween(180)) +
                    slideInVertically(animationSpec = tween(220)) { fullHeight -> -fullHeight / 4 },
            exit = slideOutVertically(animationSpec = tween(220)) { fullHeight -> -fullHeight } +
                    fadeOut(animationSpec = tween(150))
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.period is PeriodFilter.Monthly,
                    onClick = { onPeriodChange(PeriodFilter.Monthly) },
                    label = { Text(stringResource(R.string.account_detail_period_monthly)) }
                )
                FilterChip(
                    selected = state.period is PeriodFilter.Lifetime,
                    onClick = { onPeriodChange(PeriodFilter.Lifetime) },
                    label = { Text(stringResource(R.string.account_detail_period_lifetime)) }
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = stringResource(R.string.account_detail_income_title),
                    amount = state.totalIncomeMajor,
                    currencyCode = currencyCode
                ) { Icon(Icons.Default.KeyboardArrowUp, null, tint = Color(0xFF2E7D32)) }

                SummaryCard(
                    title = stringResource(R.string.account_detail_expense_title),
                    amount = state.totalExpenseMajor,
                    currencyCode = currencyCode
                ) { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFFC62828)) }

                SummaryCard(
                    title = stringResource(R.string.account_detail_balance_title),
                    amount = state.balanceMajor,
                    currencyCode = currencyCode,
                    emphasize = true
                )
            }

            if (state.insightTopCategoryId != null || state.insightMoMDeltaPct != null) {
                ElevatedCard {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.insightTopCategoryId?.let { topId ->
                            val label = state.categoryNames[topId] ?: stringResource(R.string.account_detail_no_category)
                            Text(stringResource(R.string.account_detail_insight_top_category, label))
                        }
                        state.insightMoMDeltaPct?.let { pct ->
                            val color = if (pct >= BigDecimal.ZERO) Color(0xFFC62828) else Color(0xFF2E7D32)
                            val sign = if (pct >= BigDecimal.ZERO) "+" else ""
                            val pctAbs = pct.abs().toPlainString()
                            Text(
                                text = stringResource(R.string.account_detail_insight_mom_delta, sign + pctAbs),
                                color = color
                            )
                        }
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.account_detail_chart_title), style = MaterialTheme.typography.titleMedium)
                    BarChart(state)
                }
            }

            if (state.categoryDistribution.isNotEmpty()) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(R.string.account_detail_distribution_title), style = MaterialTheme.typography.titleMedium)
                        state.categoryDistribution.take(6).forEach { dist ->
                            val label = dist.categoryId?.let { id -> state.categoryNames[id] }
                                ?: stringResource(R.string.account_detail_no_category)
                            val formatted = remember(dist.totalMajor, currencyCode) {
                                formatAmountMajorBD(dist.totalMajor, currencyCode, trimZeroDecimals = true, label = CurrencyLabel.CODE)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label, style = MaterialTheme.typography.bodyMedium)
                                Text(formatted, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.account_detail_transactions_title),
            style = MaterialTheme.typography.titleMedium
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(state.transactions) { tx -> TransactionRow(tx, state.accountCurrencyCode) }
        }
    }
}

@Composable
private fun RowScope.SummaryCard(
    title: String,
    amount: BigDecimal,
    currencyCode: String,
    emphasize: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    val text = remember(amount, currencyCode) {
        formatAmountMajorBD(amount, currencyCode, trimZeroDecimals = true, label = CurrencyLabel.CODE)
    }

    ElevatedCard(Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(
                        Modifier.size(26.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { icon() }
                    Spacer(Modifier.width(8.dp))
                }
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = text,
                style = if (emphasize) MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                else MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun BarChart(state: AccountDetailUiState, height: Dp = 140.dp) {
    val maxVal = state.monthly
        .flatMap { listOf(it.incomeMajor, it.expenseMajor) }
        .fold(BigDecimal.ZERO) { acc, v -> if (v > acc) v else acc }
        .takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE

    Column(Modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            val n = state.monthly.size
            if (n == 0) return@Canvas
            val groupW = size.width / n
            val barW = min(groupW / 4f, 18.dp.toPx())
            val baseY = size.height - 8.dp.toPx()

            state.monthly.forEachIndexed { idx, m ->
                val x = groupW * idx + groupW / 2f
                val incH = (m.incomeMajor.toFloat() / maxVal.toFloat()) * (size.height * 0.78f)
                val expH = (m.expenseMajor.toFloat() / maxVal.toFloat()) * (size.height * 0.78f)

                drawLine(Color(0xFF2E7D32), Offset(x - barW, baseY), Offset(x - barW, baseY - incH), barW, StrokeCap.Round)
                drawLine(Color(0xFFC62828), Offset(x + barW, baseY), Offset(x + barW, baseY - expH), barW, StrokeCap.Round)
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            state.monthly.forEach { m ->
                val label = m.month.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(Color(0xFF2E7D32), stringResource(R.string.account_detail_legend_income))
            LegendDot(Color(0xFFC62828), stringResource(R.string.account_detail_legend_expense))
        }
    }
}

@Composable private fun LegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TransactionRow(
    tx: Transaction,
    currencyCode: String
) {
    val amountText = remember(tx.amountMinor, currencyCode) {
        NumberUtils.formatAmountWithSymbol(tx.amountMinor, currencyCode, trimZeroDecimals = true)
    }

    val dateFmt = remember(Locale.getDefault()) {
        DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
    }

    val color = if (tx.type == TxType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)

    ElevatedCard {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = tx.merchant ?: tx.note ?: stringResource(R.string.account_detail_transaction_fallback),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = tx.date.format(dateFmt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatAmountMajorBD(
    amountMajor: BigDecimal,
    currencyCode: String,
    trimZeroDecimals: Boolean = true,
    grouping: Boolean = true,
    label: CurrencyLabel = CurrencyLabel.SYMBOL
): String {
    val currency = runCatching { Currency.getInstance(currencyCode) }
        .getOrElse { Currency.getInstance(Locale.getDefault()) }
    val digits = currency.defaultFractionDigits.coerceAtLeast(0)

    val minor = runCatching {
        amountMajor.movePointRight(digits).setScale(0, RoundingMode.HALF_UP).longValueExact()
    }.getOrNull()

    if (minor != null) {
        return when (label) {
            CurrencyLabel.SYMBOL -> NumberUtils.formatAmountWithSymbol(minor, currency.currencyCode, trimZeroDecimals)
            CurrencyLabel.CODE   -> NumberUtils.formatAmountWithCode(minor, currency.currencyCode, trimZeroDecimals)
            CurrencyLabel.NONE   -> NumberUtils.formatAmountPlain(minor, currency.currencyCode, trimZeroDecimals)
        }
    }

    val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        isGroupingUsed = grouping
        if (trimZeroDecimals && amountMajor.stripTrailingZeros().scale() <= 0) {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        } else {
            minimumFractionDigits = digits
            maximumFractionDigits = digits
        }
    }
    val absText = nf.format(amountMajor.abs())
    val prefix = when (label) {
        CurrencyLabel.SYMBOL -> currency.getSymbol(Locale.getDefault())
        CurrencyLabel.CODE   -> currency.currencyCode
        CurrencyLabel.NONE   -> ""
    }
    val sep = if (label == CurrencyLabel.NONE) "" else " "
    return if (amountMajor.signum() < 0) "-$prefix$sep$absText" else "$prefix$sep$absText"
}