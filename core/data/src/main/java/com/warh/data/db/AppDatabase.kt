package com.warh.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.warh.data.R
import com.warh.data.daos.AccountDao
import com.warh.data.daos.BudgetDao
import com.warh.data.daos.CategoryDao
import com.warh.data.daos.MerchantSuggestDao
import com.warh.data.daos.TransactionDao
import com.warh.data.entities.AccountEntity
import com.warh.data.entities.BudgetEntity
import com.warh.data.entities.CategoryEntity
import com.warh.data.entities.TransactionEntity
import com.warh.domain.models.Category
import com.warh.domain.models.TxType
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
                //TODO - Sacar las accounts+txs luego
                //db.execSQL("INSERT INTO accounts(id,name,type,currency,balance,initialBalance,iconIndex,iconColorArgb) VALUES (1,'Efectivo','CASH','ARS',12500,12500,1,null)")
                //db.execSQL("INSERT INTO accounts(id,name,type,currency,balance,initialBalance,iconIndex,iconColorArgb) VALUES (2,'Banco','BANK','USD',10,10,2,null)")

                val expenseSeeds = listOf(
                    Category(0, context.getString(R.string.cat_food),          iconIndex = 0,  iconColorArgb = 0xFFE57373, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_transport),     iconIndex = 1,  iconColorArgb = 0xFF64B5F6, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_home),          iconIndex = 2,  iconColorArgb = 0xFF81C784, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_shopping),      iconIndex = 3,  iconColorArgb = 0xFFFFB74D, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_health),        iconIndex = 4,  iconColorArgb = 0xFF4DB6AC, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_education),     iconIndex = 5,  iconColorArgb = 0xFFBA68C8, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_travel),        iconIndex = 6,  iconColorArgb = 0xFF4FC3F7, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_entertainment), iconIndex = 7,  iconColorArgb = 0xFFFF8A65, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_pets),          iconIndex = 8,  iconColorArgb = 0xFF8D6E63, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_utilities),     iconIndex = 9,  iconColorArgb = 0xFF9E9E9E, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_hobbies),       iconIndex = 10, iconColorArgb = 0xFF90A4AE, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_repairs),       iconIndex = 11, iconColorArgb = 0xFFFFD54F, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_clothing),      iconIndex = 12, iconColorArgb = 0xFF7986CB, type = TxType.EXPENSE),
                    Category(0, context.getString(R.string.cat_others),        iconIndex = 13, iconColorArgb = 0xFFBDBDBD, type = TxType.EXPENSE),
                )

                val incomeSeeds = listOf(
                    Category(0, context.getString(R.string.cat_salary),        iconIndex = 0,  iconColorArgb = 0xFF64B5F6, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_investments),   iconIndex = 1,  iconColorArgb = 0xFF7E57C2, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_bonuses),       iconIndex = 2,  iconColorArgb = 0xFF26C6DA, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_savings),       iconIndex = 3,  iconColorArgb = 0xFF66BB6A, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_gifts),         iconIndex = 4,  iconColorArgb = 0xFFFFB74D, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_rentals),       iconIndex = 5,  iconColorArgb = 0xFF8D6E63, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_sales),         iconIndex = 6,  iconColorArgb = 0xFFEC407A, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_refunds),       iconIndex = 7,  iconColorArgb = 0xFF9CCC65, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_awards),        iconIndex = 8,  iconColorArgb = 0xFFFFCA28, type = TxType.INCOME),
                    Category(0, context.getString(R.string.cat_others),        iconIndex = 9,  iconColorArgb = 0xFFBDBDBD, type = TxType.INCOME),
                )

                var nextId = 1L
                fun insertAll(list: List<Category>) {
                    list.forEach { c ->
                        db.execSQL(
                            "INSERT INTO categories(id,name,iconIndex,iconColorArgb,type) VALUES (?,?,?,?,?)",
                            arrayOf(nextId++, c.name, c.iconIndex, c.iconColorArgb ?: 0, c.type.name)
                        )
                    }
                }

                insertAll(expenseSeeds)
                insertAll(incomeSeeds)

                val now = LocalDateTime.now()
                val nowStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                val ymStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM"))

                /*db.execSQL(
                    """
                    INSERT INTO transactions(id,accountId,type,amountMinor,currency,date,yearMonth,categoryId,merchant,note) VALUES
                    (1,1,'INCOME',25000,'ARS','$nowStr','$ymStr',15,'Sueldo','Sueldo del trabajo'),
                    (2,1,'EXPENSE',12500,'ARS','$nowStr','$ymStr',1,'Café','Latte y medialuna'),
                    (3,2,'INCOME',10000,'USD','$nowStr','$ymStr',17,'Transferencia','Recibo de dólares')
                    """.trimIndent()
                )*/
            }
        })
        .addMigrations()
        .build()