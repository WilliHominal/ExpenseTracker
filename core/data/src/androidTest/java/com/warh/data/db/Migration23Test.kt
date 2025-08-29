package com.warh.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.warh.data.db.utils.MigrationTestHelpers.assertColumnExists
import com.warh.data.db.utils.MigrationTestHelpers.assertColumnNotExists
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration23Test {

    private val dbName = "expense-migration-23-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate2to3_removesCurrencyAndKeepsData() {
        // 1) Crea DB en versión 2 con el schema exportado y mete datos de prueba
        helper.createDatabase(dbName, 2).apply {
            execSQL("""
                INSERT INTO accounts (id, name, type, currency, balance, initialBalance, iconIndex, iconColorArgb)
                VALUES (1, 'Cash', 'CASH', 'USD', 0, 0, 1, NULL)
            """.trimIndent())

            execSQL("""
                INSERT INTO transactions(
                    id, accountId, type, amountMinor, currency, date, yearMonth, categoryId, merchant, note
                ) VALUES (
                    100, 1, 'EXPENSE', 9999, 'USD', '2025-08-01T10:00', '2025-08', NULL, 'Test Store', 'Before migration'
                )
            """.trimIndent())
            close()
        }

        // 2) Migra y valida contra 3.json
        val db = helper.runMigrationsAndValidate(
            name = dbName,
            version = 3,
            validateDroppedTables = true
        )

        // 3) 'currency' no debe existir
        db.query("PRAGMA table_info(transactions)").use { c ->
            assertColumnNotExists(c, "currency")
            assertColumnExists(c, "amountMinor")
            assertColumnExists(c, "merchant")
            assertColumnExists(c, "date")
            assertColumnExists(c, "accountId")
        }

        // 4) La fila migra con los demás datos intactos
        db.query("""
            SELECT id, accountId, type, amountMinor, date, yearMonth, categoryId, merchant, note
            FROM transactions WHERE id = 100
        """.trimIndent()).use { c ->
            check(c.moveToFirst()) { "No se encontró la fila migrada en transactions." }
            val amount = c.getLong(c.getColumnIndexOrThrow("amountMinor"))
            val merchant = c.getString(c.getColumnIndexOrThrow("merchant"))
            val note = c.getString(c.getColumnIndexOrThrow("note"))
            check(amount == 9999L)
            check(merchant == "Test Store")
            check(note == "Before migration")
        }
    }
}