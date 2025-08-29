package com.warh.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.warh.data.db.migrations.MIGRATION_1_2
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration12Test {

    private val dbName = "expense-migration-12-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    /**
     * Caso principal:
     * - En v1 creamos:
     *   a) 1 cuenta SIN id explícito (id NULL/auto)    -> debe copiarse a accounts_new con id autogenerado
     *   b) 1 cuenta con id=10                          -> se preserva el id
     *   c) 1 transacción huérfana (accountId = 0/999)  -> DEBE borrarse por el DELETE previo a recrear la tabla
     *   d) 1 transacción válida (accountId = 10)       -> DEBE conservarse
     */
    @Test
    fun migrate1to2_cleansOrphans_and_keepsValidRows() {
        // Crea DB en versión 1 con el schema exportado y mete datos de prueba
        helper.createDatabase(dbName, 1).apply {
            // Cuentas
            // a) cuenta sin id explícito (id NULL o AUTOINCREMENT)
            execSQL("""
                INSERT INTO accounts (name, type, currency, balance, initialBalance, iconIndex, iconColorArgb)
                VALUES ('Cash v1', 'CASH', 'USD', 0, 0, 1, NULL)
            """.trimIndent())

            // b) cuenta con id=10 (válida)
            execSQL("""
                INSERT INTO accounts (id, name, type, currency, balance, initialBalance, iconIndex, iconColorArgb)
                VALUES (10, 'Bank v1', 'BANK', 'USD', 0, 0, 2, NULL)
            """.trimIndent())

            // Transacciones
            // c) huérfana (accountId que no existe en accounts v1)
            execSQL("""
                INSERT INTO transactions(
                    id, accountId, type, amountMinor, currency, date, yearMonth, categoryId, merchant, note
                ) VALUES (
                    1000, 999, 'EXPENSE', 5000, 'USD', '2025-07-01T10:00', '2025-07', NULL, 'Orphan Store', 'orphan row'
                )
            """.trimIndent())

            // d) válida (apunta a id=10)
            execSQL("""
                INSERT INTO transactions(
                    id, accountId, type, amountMinor, currency, date, yearMonth, categoryId, merchant, note
                ) VALUES (
                    1001, 10, 'INCOME', 12345, 'USD', '2025-08-01T09:00', '2025-08', NULL, 'Valid Inc', 'kept'
                )
            """.trimIndent())

            close()
        }

        // 2) Migra y valida contra 2.json
        val db = helper.runMigrationsAndValidate(
            name = dbName,
            version = 2,
            validateDroppedTables = true,
            MIGRATION_1_2
        )

        // ------- Validaciones -------
        // 1) 'accounts' no tiene filas con id=0 y existe la de id=10
        db.query("SELECT id, name FROM accounts").use { c ->
            var hasId10 = false
            var hasId0 = false
            while (c.moveToNext()) {
                val id = c.getLong(c.getColumnIndexOrThrow("id"))
                if (id == 10L) hasId10 = true
                if (id == 0L) hasId0 = true
            }
            check(hasId10) { "La cuenta con id=10 debería existir tras la migración." }
            check(!hasId0) { "No debería haber cuentas con id=0 tras la migración." }
        }

        // 2) La transacción huérfana debe haber sido eliminada
        db.query("SELECT COUNT(*) AS cnt FROM transactions WHERE id = 1000").use { c ->
            check(c.moveToFirst())
            val cnt = c.getLong(c.getColumnIndexOrThrow("cnt"))
            check(cnt == 0L) { "La transacción huérfana (id=1000) no fue eliminada." }
        }

        // 3) La transacción válida (id=1001, accountId=10) debe existir
        db.query("""
            SELECT id, accountId, type, amountMinor, currency, merchant, note
            FROM transactions WHERE id = 1001
        """.trimIndent()).use { c ->
            check(c.moveToFirst()) { "La transacción válida (id=1001) no está tras la migración." }
            val accId = c.getLong(c.getColumnIndexOrThrow("accountId"))
            val amount = c.getLong(c.getColumnIndexOrThrow("amountMinor"))
            val merchant = c.getString(c.getColumnIndexOrThrow("merchant"))
            val note = c.getString(c.getColumnIndexOrThrow("note"))
            check(accId == 10L) { "accountId debería ser 10; fue $accId" }
            check(amount == 12345L) { "amountMinor debería ser 12345; fue $amount" }
            check(merchant == "Valid Inc")
            check(note == "kept")
        }

        // 4) La PK de accounts es AUTOINCREMENT y la secuencia está seteada
        db.query("SELECT name, seq FROM sqlite_sequence WHERE name = 'accounts'").use { c ->
            check(c.moveToFirst()) { "sqlite_sequence no contiene entrada para 'accounts' tras la migración." }
        }
    }

    /**
     * Índices recreados correctamente en 'transactions'.
     */
    @Test
    fun migrate1to2_indexesRecreated() {
        helper.createDatabase(dbName, 1).close()
        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        db.query("PRAGMA index_list('transactions')").use { c ->
            val indexes = mutableSetOf<String>()
            val idxName = c.getColumnIndexOrThrow("name")
            while (c.moveToNext()) indexes.add(c.getString(idxName))

            val expected = setOf(
                "index_transactions_type_categoryId_date",
                "index_transactions_accountId_date",
                "index_transactions_accountId",
                "index_transactions_date",
                "index_transactions_merchant"
            )
            check(indexes.containsAll(expected)) {
                "Faltan índices en 'transactions'. Esperados=$expected, actuales=$indexes"
            }
        }
    }
}