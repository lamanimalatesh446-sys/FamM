package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val timestamp: Long,
    val category: String, // "Food", "Shopping", "Gaming", "Entertainment", "Outings", "Others"
    val isDebited: Boolean,
    val status: String // "SUCCESS", "PENDING", "FAILED"
)

@Entity(tableName = "settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val walletBalance: Double = 5250.0, // Initial pocket money for the teen!
    val upiId: String = "aditya@fam",
    val cardHolderName: String = "Aditya Sharma",
    val upiPin: String = "1234",
    val kycVerified: Boolean = false,
    val kycAadharNumber: String = "",
    val spendingLimitDaily: Double = 1000.0,
    val spendingSpentToday: Double = 0.0,
    val parentalApprovalRequired: Boolean = true,
    val parentLinkedContact: String = "+91 98765 43210",
    val cardBlocked: Boolean = false,
    val cardTapEnabled: Boolean = true,
    val cardCvvMock: String = "349",
    val cardExpiry: String = "12/30",
    val cardNo: String = "4815 1623 4210 8899"
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettings?>

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)

    @Update
    suspend fun updateSettings(settings: AppSettings)
}

@Database(entities = [Transaction::class, AppSettings::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun appSettingsDao(): AppSettingsDao
}

class WalletRepository(
    private val transactionDao: TransactionDao,
    private val appSettingsDao: AppSettingsDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val appSettings: Flow<AppSettings?> = appSettingsDao.getSettingsFlow()

    suspend fun getSettingsDirect(): AppSettings {
        var settings = appSettingsDao.getSettings()
        if (settings == null) {
            settings = AppSettings()
            appSettingsDao.insertSettings(settings)
        }
        return settings
    }

    suspend fun updateSettings(settings: AppSettings) {
        appSettingsDao.updateSettings(settings)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun seedMockTransactionsIfEmpty(count: Int) {
        // Just mock initial history to look beautiful
        val mockData = listOf(
            Transaction(
                title = "Steam Games Purchase",
                amount = 450.0,
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                category = "Gaming",
                isDebited = true,
                status = "SUCCESS"
            ),
            Transaction(
                title = "Starbucks Chocolate Shake",
                amount = 220.0,
                timestamp = System.currentTimeMillis() - 3600000 * 12,
                category = "Food",
                isDebited = true,
                status = "SUCCESS"
            ),
            Transaction(
                title = "Received pocket money",
                amount = 2000.0,
                timestamp = System.currentTimeMillis() - 3600000 * 24,
                category = "Others",
                isDebited = false,
                status = "SUCCESS"
            ),
            Transaction(
                title = "Nike Shoes & Socks Outlets",
                amount = 1500.0,
                timestamp = System.currentTimeMillis() - 3600000 * 48,
                category = "Shopping",
                isDebited = true,
                status = "SUCCESS"
            ),
            Transaction(
                title = "PVR Cinema Ticket Outing",
                amount = 350.0,
                timestamp = System.currentTimeMillis() - 3600000 * 72,
                category = "Entertainment",
                isDebited = true,
                status = "SUCCESS"
            )
        )
        for (item in mockData) {
            transactionDao.insertTransaction(item)
        }
    }
}
