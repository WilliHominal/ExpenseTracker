package com.warh.data.db.utils

import android.database.Cursor

object MigrationTestHelpers {
    @Suppress("SameParameterValue")
    fun assertColumnExists(cursor: Cursor, colName: String) {
        var exists = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == colName) { exists = true; break }
        }
        check(exists) { "La columna '$colName' debería existir en 'transactions'." }
        cursor.moveToPosition(-1)
    }

    @Suppress("SameParameterValue")
    fun assertColumnNotExists(cursor: Cursor, colName: String) {
        var exists = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == colName) { exists = true; break }
        }
        check(!exists) { "La columna '$colName' no debería existir en 'transactions' después de la migración." }
        cursor.moveToPosition(-1)
    }
}