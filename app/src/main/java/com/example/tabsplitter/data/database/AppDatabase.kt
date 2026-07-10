package com.example.tabsplitter.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tabsplitter.data.converter.Converters
import com.example.tabsplitter.data.dao.FriendDao
import com.example.tabsplitter.data.dao.TabDao
import com.example.tabsplitter.data.dao.TransactionDao
import com.example.tabsplitter.data.dao.CategoryDao
import com.example.tabsplitter.data.dao.SplitBillDao
import com.example.tabsplitter.data.entity.Friend
import com.example.tabsplitter.data.entity.Tab
import com.example.tabsplitter.data.entity.Transaction
import com.example.tabsplitter.data.entity.Category
import com.example.tabsplitter.data.entity.SplitBill
import com.example.tabsplitter.data.entity.SplitBillParticipant

@Database(
    entities = [Friend::class, Tab::class, Transaction::class, Category::class, SplitBill::class, SplitBillParticipant::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun friendDao(): FriendDao
    abstract fun tabDao(): TabDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun splitBillDao(): SplitBillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create categories table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `colorHex` TEXT NOT NULL
                    )
                """)
                
                // 2. Pre-populate default categories
                db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Food', '#FF7043')")
                db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Travel', '#29B6F6')")
                db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Shopping', '#66BB6A')")
                db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Entertainment', '#AB47BC')")
                db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Utilities', '#FFEE58')")
                db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Other', '#90A4AE')")

                // 3. Create transactions_new table with categoryId
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `tabId` INTEGER NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `description` TEXT NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        `direction` TEXT NOT NULL, 
                        `paymentStatus` TEXT NOT NULL,
                        `categoryId` INTEGER,
                        FOREIGN KEY(`tabId`) REFERENCES `tabs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """)
                
                // 4. Copy data from old transactions to transactions_new
                db.execSQL("""
                    INSERT INTO `transactions_new` (`id`, `tabId`, `amount`, `description`, `createdAt`, `direction`, `paymentStatus`, `categoryId`)
                    SELECT `id`, `tabId`, `amount`, `description`, `createdAt`, `direction`, `paymentStatus`, NULL FROM `transactions`
                """)
                
                // 5. Drop old table
                db.execSQL("DROP TABLE `transactions`")
                
                // 6. Rename new table
                db.execSQL("ALTER TABLE `transactions_new` RENAME TO `transactions`")
                
                // 7. Create indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_tabId` ON `transactions` (`tabId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` ON `transactions` (`categoryId`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `split_bills` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `description` TEXT NOT NULL, 
                        `totalAmount` REAL NOT NULL, 
                        `categoryId` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `split_bill_participants` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `splitBillId` INTEGER NOT NULL, 
                        `friendId` INTEGER NOT NULL, 
                        `shareAmount` REAL NOT NULL, 
                        `paymentStatus` TEXT NOT NULL,
                        FOREIGN KEY(`splitBillId`) REFERENCES `split_bills`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`friendId`) REFERENCES `friends`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_split_bills_categoryId` ON `split_bills` (`categoryId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_split_bill_participants_splitBillId` ON `split_bill_participants` (`splitBillId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_split_bill_participants_friendId` ON `split_bill_participants` (`friendId`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tab_splitter_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Food', '#FF7043')")
                        db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Travel', '#29B6F6')")
                        db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Shopping', '#66BB6A')")
                        db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Entertainment', '#AB47BC')")
                        db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Utilities', '#FFEE58')")
                        db.execSQL("INSERT INTO `categories` (`name`, `colorHex`) VALUES ('Other', '#90A4AE')")
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
