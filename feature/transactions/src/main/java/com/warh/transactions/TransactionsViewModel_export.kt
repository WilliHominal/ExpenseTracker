package com.warh.transactions

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warh.commons.Strings
import com.warh.commons.get
import com.warh.domain.models.TransactionFilter
import com.warh.domain.repositories.TransactionRepository
import kotlinx.coroutines.launch
import java.io.File
import java.time.format.DateTimeFormatter

class TransactionsExportViewModel(
    private val repo: TransactionRepository,
    private val strings: Strings
) : ViewModel() {
    fun exportCsv(context: Context, filter: TransactionFilter) {
        viewModelScope.launch {
            val txs = repo.list(filter)
            val csv = buildString {
                appendLine(strings[R.string.csv_headers])
                val df = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                txs.forEach { t ->
                    fun esc(s: String?) = if (s == null) "" else '"' + s.replace("\"", "\"\"") + '"'
                    appendLine(
                        listOf(
                            t.id, df.format(t.date), t.type, t.accountId, t.categoryId ?: "",
                            t.amountMinor, esc(t.merchant), esc(t.note)
                        ).joinToString(",")
                    )
                }
            }
            val file = File(context.cacheDir, "${strings[R.string.csv_name]}.csv").apply { writeText(csv, Charsets.UTF_8) }
            val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, strings[R.string.export_csv_title]))
        }
    }
}