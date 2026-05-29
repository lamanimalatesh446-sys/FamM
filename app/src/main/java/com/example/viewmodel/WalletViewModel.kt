package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.AppSettings
import com.example.data.Transaction
import com.example.data.WalletRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class KycState {
    object Initial : KycState()
    object SendingOtp : KycState()
    object OtpSent : KycState()
    object Verifying : KycState()
    object Success : KycState()
    data class Error(val message: String) : KycState()
}

sealed class PaymentState {
    object Idle : PaymentState()
    data class LimitExceeded(val required: Double, val maxAllowed: Double) : PaymentState()
    data class ParentalApprovalPending(val title: String, val amount: Double, val category: String) : PaymentState()
    object Processing : PaymentState()
    object Success : PaymentState()
    data class Error(val message: String) : PaymentState()
}

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "famteen_pay_db"
    ).fallbackToDestructiveMigration().build()

    private val repository = WalletRepository(db.transactionDao(), db.appSettingsDao())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings: StateFlow<AppSettings?> = repository.appSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _kycProgress = MutableStateFlow<KycState>(KycState.Initial)
    val kycProgress = _kycProgress.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState = _paymentState.asStateFlow()

    init {
        viewModelScope.launch {
            // Seed settings
            val directSettings = repository.getSettingsDirect()
            // Seed transactions if empty
            val currentTxs = repository.allTransactions.first()
            if (currentTxs.isEmpty()) {
                repository.seedMockTransactionsIfEmpty(5)
            }
        }
    }

    // Add Simulated Pocket Money from Parents
    fun addPocketMoney(amount: Double) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            val updated = current.copy(
                walletBalance = current.walletBalance + amount
            )
            repository.updateSettings(updated)
            
            // Add custom transaction for pocket money
            repository.insertTransaction(
                Transaction(
                    title = "Pocket Money Added by Parent",
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    category = "Others",
                    isDebited = false,
                    status = "SUCCESS"
                )
            )
        }
    }

    // Toggle Card Statuses
    fun toggleCardBlockState() {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            val updated = current.copy(cardBlocked = !current.cardBlocked)
            repository.updateSettings(updated)
        }
    }

    fun toggleCardTapAndPay() {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            val updated = current.copy(cardTapEnabled = !current.cardTapEnabled)
            repository.updateSettings(updated)
        }
    }

    fun updateCardHolderName(newName: String) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            val updated = current.copy(cardHolderName = if (newName.isNotBlank()) newName else "Aditya Sharma")
            repository.updateSettings(updated)
        }
    }

    // Parental Limit Updates
    fun updateDailyLimit(newLimit: Double) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            val updated = current.copy(spendingLimitDaily = newLimit)
            repository.updateSettings(updated)
        }
    }

    fun updateParentalSettings(approvalRequired: Boolean, contact: String) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            val updated = current.copy(
                parentalApprovalRequired = approvalRequired,
                parentLinkedContact = contact
            )
            repository.updateSettings(updated)
        }
    }

    fun resetDailySpending() {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()
            val updated = current.copy(spendingSpentToday = 0.0)
            repository.updateSettings(updated)
        }
    }

    // Aadhar KYC simulation process
    fun startKyc(aadharNumber: String) {
        if (aadharNumber.length != 12 || !aadharNumber.all { it.isDigit() }) {
            _kycProgress.value = KycState.Error("Aadhar can only contain 12 numeric digits.")
            return
        }

        viewModelScope.launch {
            _kycProgress.value = KycState.SendingOtp
            delay(1500)
            // Save temporary aadhar but keep kycVerified false until otp verified
            val current = repository.getSettingsDirect()
            val updated = current.copy(kycAadharNumber = aadharNumber)
            repository.updateSettings(updated)
            _kycProgress.value = KycState.OtpSent
        }
    }

    fun verifyKycOtp(otp: String) {
        if (otp != "123456" && otp != "654321") {
            // A quick demo hint: type 123456 to verify!
            _kycProgress.value = KycState.Error("Invalid OTP code. Enter '123456' for demo verification.")
            return
        }

        viewModelScope.launch {
            _kycProgress.value = KycState.Verifying
            delay(1500)
            val current = repository.getSettingsDirect()
            val updated = current.copy(kycVerified = true)
            repository.updateSettings(updated)
            _kycProgress.value = KycState.Success
        }
    }

    fun resetKycState() {
        _kycProgress.value = KycState.Initial
    }

    // Payment Flows
    fun initiatePayment(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect()

            // 1. Check if card/wallet is blocked
            if (current.cardBlocked && title == "Physical Card Tap") {
                _paymentState.value = PaymentState.Error("Your card is currently blocked! Unlock it first.")
                return@launch
            }

            // 2. Check general Balance
            if (current.walletBalance < amount) {
                _paymentState.value = PaymentState.Error("Insufficient wallet balance. Ask parent for Pocket Money!")
                return@launch
            }

            // 3. Check Teen Spending Limit constraint
            if (current.spendingSpentToday + amount > current.spendingLimitDaily) {
                _paymentState.value = PaymentState.LimitExceeded(
                    required = current.spendingSpentToday + amount,
                    maxAllowed = current.spendingLimitDaily
                )
                return@launch
            }

            // 4. Parental Approval needed for amounts above Rs 500
            if (current.parentalApprovalRequired && amount > 500.0) {
                _paymentState.value = PaymentState.ParentalApprovalPending(title, amount, category)
            } else {
                // Instantly complete
                executePayment(title, amount, category)
            }
        }
    }

    fun approveParentalPending() {
        val state = _paymentState.value
        if (state is PaymentState.ParentalApprovalPending) {
            executePayment(state.title, state.amount, state.category)
        }
    }

    fun rejectParentalPending() {
        _paymentState.value = PaymentState.Error("Transaction denied by Parent's spending protection rules.")
    }

    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
    }

    private fun executePayment(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Processing
            delay(1200)

            val current = repository.getSettingsDirect()
            if (current.walletBalance < amount) {
                _paymentState.value = PaymentState.Error("Insufficient balance.")
                return@launch
            }

            val updated = current.copy(
                walletBalance = current.walletBalance - amount,
                spendingSpentToday = current.spendingSpentToday + amount
            )
            repository.updateSettings(updated)

            // Save transaction to history database
            repository.insertTransaction(
                Transaction(
                    title = title,
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    category = category,
                    isDebited = true,
                    status = "SUCCESS"
                )
            )

            _paymentState.value = PaymentState.Success
        }
    }
}
