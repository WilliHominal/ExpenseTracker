package com.warh.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.material3.ColorProviders
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.warh.designsystem.DarkScheme
import com.warh.designsystem.LightScheme
import com.warh.expensetracker.R
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class BalanceWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode: SizeMode = SizeMode.Exact

    object Keys { val Balances = stringPreferencesKey("balances_payload") }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(
                colors = ColorProviders(
                    light = LightScheme,
                    dark = DarkScheme)
            ) {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = currentState<Preferences>()
        val payload = prefs[Keys.Balances].orEmpty()

        val items = payload.split("|").mapNotNull { token ->
            val p = token.split(":")
            if (p.size == 2) p[0] to (p[1].toLongOrNull() ?: return@mapNotNull null) else null
        }

        val openApp = actionStartActivity(
            Intent(Intent.ACTION_VIEW)
                .setClassName("com.warh.expensetracker", "com.warh.expensetracker.MainActivity")
        )

        Column(
            modifier = GlanceModifier
                .appWidgetBackground()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(16.dp)
                .padding(10.dp)
                .fillMaxSize()
                .clickable(openApp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = context.getString(R.string.widget_balance_title),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(GlanceModifier.height(2.dp))

            if (items.isEmpty()) {
                Text(
                    text = context.getString(R.string.widget_empty),
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
                    maxLines = 1
                )
            } else {
                val show = items.take(4)
                show.forEach { (code, minor) -> LineCurrency(code, minor, 11.sp) }
            }
        }
    }

    @Composable
    private fun LineCurrency(code: String, minor: Long, fontSize: TextUnit) {
        val digits = runCatching { Currency.getInstance(code).defaultFractionDigits }
            .getOrDefault(2).coerceAtLeast(0)
        val major = BigDecimal(minor).movePointLeft(digits)
        val nf = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            maximumFractionDigits = digits
            minimumFractionDigits = digits
            isGroupingUsed = false
        }

        Row(GlanceModifier.fillMaxWidth()) {
            Text(
                text = code,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )

            Spacer(GlanceModifier.defaultWeight())

            Text(
                text = nf.format(major),
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
        }
    }
}