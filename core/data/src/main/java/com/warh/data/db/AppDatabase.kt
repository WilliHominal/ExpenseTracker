package com.warh.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.warh.data.daos.AccountDao
import com.warh.data.daos.BudgetDao
import com.warh.data.daos.CategoryDao
import com.warh.data.daos.MerchantSuggestDao
import com.warh.data.daos.TransactionDao
import com.warh.data.entities.AccountEntity
import com.warh.data.entities.BudgetEntity
import com.warh.data.entities.CategoryEntity
import com.warh.data.entities.TransactionEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun merchantSuggestDao(): MerchantSuggestDao
}

fun buildDatabase(context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, "expense.db")
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                //TODO - Sacar luego: Seed solo inicial
                db.execSQL("INSERT INTO accounts(id,name,type,currency,balanceMinor,iconIndex,iconColorArgb) VALUES (1,'Efectivo','CASH','ARS',12500,1,null)")
                db.execSQL("INSERT INTO accounts(id,name,type,currency,balanceMinor,iconIndex,iconColorArgb) VALUES (2,'Banco','BANK','USD',0,2,null)")

                db.execSQL("INSERT INTO categories(id,name,colorArgb) VALUES (1,'Comida',0xFFE57373)")
                db.execSQL("INSERT INTO categories(id,name,colorArgb) VALUES (2,'Transporte',0xFF64B5F6)")
                db.execSQL("INSERT INTO categories(id,name,colorArgb) VALUES (3,'Hogar',0xFF81C784)")

                val now = LocalDateTime.now()
                val nowStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val ymStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))

                db.execSQL(
                    """
                    INSERT INTO transactions(id,accountId,type,amountMinor,currency,date,yearMonth,categoryId,merchant,note) VALUES
                    (1001,1,'INCOME',25000,'ARS','$nowStr','$ymStr',3,'Sueldo','Sueldo del trabajo'),
                    (1002,1,'EXPENSE',12500,'ARS','$nowStr','$ymStr',1,'Café','Latte y medialuna'),
                    (1003,2,'INCOME',10000,'USD','$nowStr','$ymStr',3,'Transferencia','Recibo de dólares')
                    """.trimIndent()
                )
            }
        })
        .addMigrations()
        .build()