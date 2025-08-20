package com.warh.expensetracker.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.warh.data.db.buildDatabase

class BalanceWidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val db = buildDatabase(applicationContext)
        val rows = db.accountDao().totalsByCurrency()

        val payload = rows.joinToString("|") { "${it.currency}:${it.totalMinor}" }

        val manager = GlanceAppWidgetManager(applicationContext)
        val glanceIds = manager.getGlanceIds(BalanceWidget::class.java)

        glanceIds.forEach { id ->
            updateAppWidgetState(applicationContext, id) { prefs ->
                if (rows.isEmpty()) {
                    prefs.remove(BalanceWidget.Keys.Balances)
                } else {
                    prefs[BalanceWidget.Keys.Balances] = payload
                }
            }
        }

        BalanceWidget().updateAll(applicationContext)

        Result.success()
    } catch (e: Throwable) {
        Result.retry()
    }

    companion object {
        fun enqueue(context: Context) {
            val req = OneTimeWorkRequestBuilder<BalanceWidgetRefreshWorker>().build()
            WorkManager.getInstance(context).enqueue(req)
        }
    }
}