package com.warh.accounts.details

import androidx.compose.animation.animateContentSize
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
import com.warh.commons.TopBarDefault
import com.warh.domain.models.Transaction
import com.warh.domain.models.TxType
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

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
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(12.dp))
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
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("es","AR")) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            SummaryCard(stringResource(R.string.account_detail_income_title), state.totalIncomeMajor, currency) {
                Icon(Icons.Default.KeyboardArrowUp, null, tint = Color(0xFF2E7D32))
            }
            SummaryCard(stringResource(R.string.account_detail_expense_title), state.totalExpenseMajor, currency) {
                Icon(Icons.Default.KeyboardArrowDown, null, tint = Color(0xFFC62828))
            }
            SummaryCard(stringResource(R.string.account_detail_balance_title), state.balanceMajor, currency, emphasize = true)
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

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                            Text(currency.format(dist.totalMajor), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        Text(stringResource(R.string.account_detail_transactions_title), style = MaterialTheme.typography.titleMedium)
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(state.transactions) { tx -> TransactionRow(tx) }
        }
    }
}

@Composable
private fun RowScope.SummaryCard(
    title: String,
    amount: BigDecimal,
    currency: NumberFormat,
    emphasize: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    ElevatedCard(Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(Modifier.size(26.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) { icon() }
                    Spacer(Modifier.width(8.dp))
                }
                Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = currency.format(amount),
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
                val label = m.month.month.getDisplayName(TextStyle.SHORT, Locale("es","AR"))
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
private fun TransactionRow(tx: Transaction) {
    val currency = remember { NumberFormat.getCurrencyInstance(Locale("es","AR")) }

    val major = BigDecimal.valueOf(tx.amountMinor).movePointLeft(2)

    val color = if (tx.type == TxType.INCOME) Color(0xFF2E7D32)
    else Color(0xFFC62828)

    val df = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }

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
                    text = tx.date.format(df),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = currency.format(major),
                style = MaterialTheme.typography.bodyLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}