package com.warh.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS accounts_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                currency TEXT NOT NULL,
                balance INTEGER NOT NULL,
                initialBalance INTEGER NOT NULL,
                iconIndex INTEGER NOT NULL,
                iconColorArgb INTEGER
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO accounts_new (id, name, type, currency, balance, initialBalance, iconIndex, iconColorArgb)
            SELECT 
                CASE WHEN id IS NULL OR id = 0 THEN NULL ELSE id END,
                name, type, currency, balance, initialBalance, iconIndex, iconColorArgb
            FROM accounts
        """.trimIndent())

        db.execSQL("""
            INSERT OR REPLACE INTO sqlite_sequence (name, seq)
            SELECT 'accounts', IFNULL(MAX(id), 0) FROM accounts_new
        """.trimIndent())

        db.execSQL("""
            DELETE FROM transactions
            WHERE accountId NOT IN (SELECT id FROM accounts_new)
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS transactions_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                accountId INTEGER NOT NULL,
                type TEXT NOT NULL,
                amountMinor INTEGER NOT NULL,
                currency TEXT NOT NULL,
                date TEXT NOT NULL,
                yearMonth TEXT NOT NULL,
                categoryId INTEGER,
                merchant TEXT,
                note TEXT,
                FOREIGN KEY(accountId) REFERENCES accounts(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO transactions_new (
                id, accountId, type, amountMinor, currency, date, yearMonth, categoryId, merchant, note
            )
            SELECT id, accountId, type, amountMinor, currency, date, yearMonth, categoryId, merchant, note
            FROM transactions
        """.trimIndent())

        db.execSQL("DROP TABLE transactions")
        db.execSQL("DROP TABLE accounts")
        db.execSQL("ALTER TABLE accounts_new RENAME TO accounts")
        db.execSQL("ALTER TABLE transactions_new RENAME TO transactions")

        db.execSQL("""CREATE INDEX IF NOT EXISTS index_transactions_type_categoryId_date ON transactions(type, categoryId, date)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS index_transactions_accountId_date     ON transactions(accountId, date)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS index_transactions_accountId          ON transactions(accountId)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS index_transactions_date               ON transactions(date)""")
        db.execSQL("""CREATE INDEX IF NOT EXISTS index_transactions_merchant           ON transactions(merchant)""")
    }
}