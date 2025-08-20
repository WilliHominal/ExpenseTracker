package com.warh.expensetracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.warh.expensetracker.R
import java.math.BigDecimal
import java.util.Currency

class BalanceWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    object Keys { val Balances = stringPreferencesKey("balances_payload") }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = currentState<Preferences>()
        val payload = prefs[Keys.Balances].orEmpty()
        val items = payload
            .split("|")
            .mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size == 2) {
                    val minor = parts[1].toLongOrNull() ?: return@mapNotNull null
                    parts[0] to minor
                } else null
            }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
        ) {

            Text(
                text = context.getString(R.string.widget_balance_title),
                style = TextStyle(fontWeight = FontWeight.Bold)
            )

            if (items.isEmpty()) {
                Text(text = context.getString(R.string.widget_empty))
            } else {
                items.forEach { (code, minor) ->
                    val digits = runCatching { Currency.getInstance(code).defaultFractionDigits }
                        .getOrDefault(2).coerceAtLeast(0)
                    val major = BigDecimal(minor).movePointLeft(digits)
                    Text(text = "$code  $major")
                }
            }

            Spacer(GlanceModifier.height(8.dp))

            Row(horizontalAlignment = Alignment.End, modifier = GlanceModifier.fillMaxWidth()) {
                Button(
                    text = context.getString(R.string.widget_open),
                    onClick = actionStartActivity(
                        Intent(Intent.ACTION_VIEW)
                            .setClassName(
                                "com.warh.expensetracker",
                                "com.warh.expensetracker.MainActivity"
                            )
                    )
                )
                Spacer(GlanceModifier.width(6.dp))
                Button(
                    text = context.getString(R.string.widget_refresh),
                    onClick = actionRunCallback<RefreshAction>()
                )
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        BalanceWidgetRefreshWorker.enqueue(context)
    }
}

/*class BalanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long running
        // operations.

        provideContent {
            // create your AppWidget here
            MyContent()
        }
    }

    @Composable
    private fun MyContent() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Where to?", modifier = GlanceModifier.padding(12.dp))
            Row(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    text = "Home",
                    onClick = actionStartActivity<MainActivity>()
                )
                Button(
                    text = "Work",
                    onClick = actionStartActivity<MainActivity>()
                )
            }
        }
    }
}*/