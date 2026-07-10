package com.example.tabsplitter.ui

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tabsplitter.MainActivity
import com.example.tabsplitter.data.dao.FriendDao
import com.example.tabsplitter.data.dao.TabDao
import com.example.tabsplitter.data.dao.TransactionDao
import com.example.tabsplitter.data.dao.CategoryDao
import com.example.tabsplitter.data.dao.SplitBillDao
import com.example.tabsplitter.data.entity.Friend
import com.example.tabsplitter.data.entity.PaymentStatus
import com.example.tabsplitter.data.entity.Tab
import com.example.tabsplitter.data.entity.TabStatus
import com.example.tabsplitter.data.entity.Transaction
import com.example.tabsplitter.data.entity.TransactionDirection
import com.example.tabsplitter.data.entity.Category
import com.example.tabsplitter.data.entity.SplitBill
import com.example.tabsplitter.data.entity.SplitBillParticipant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class TabWithFriend(
    val tab: Tab,
    val friendName: String
)

data class CategorySpending(
    val category: Category,
    val totalAmount: Double
)

data class DetailedTransaction(
    val transaction: Transaction,
    val friendName: String,
    val category: Category?
)

data class MonthlyTotals(
    val totalSpent: Double,
    val totalYouOwe: Double,
    val totalOwedToYou: Double
)

data class SplitBillUiItem(
    val splitBill: SplitBill,
    val participantCount: Int,
    val status: String
)

data class ParticipantDetail(
    val participant: SplitBillParticipant,
    val friendName: String
)

data class SplitBillDetail(
    val splitBill: SplitBill,
    val category: Category?,
    val participants: List<ParticipantDetail>
)

class TabSplitterViewModel(
    private val friendDao: FriendDao,
    private val tabDao: TabDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val splitBillDao: SplitBillDao
) : ViewModel() {

    private val _currentYearMonth = MutableStateFlow(java.util.Calendar.getInstance().let {
        Pair(it.get(java.util.Calendar.YEAR), it.get(java.util.Calendar.MONTH))
    })
    val currentYearMonth = _currentYearMonth.asStateFlow()

    fun selectPreviousMonth() {
        val (year, month) = _currentYearMonth.value
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month)
            add(java.util.Calendar.MONTH, -1)
        }
        _currentYearMonth.value = Pair(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH))
    }

    fun selectNextMonth() {
        val (year, month) = _currentYearMonth.value
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month)
            add(java.util.Calendar.MONTH, 1)
        }
        _currentYearMonth.value = Pair(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH))
    }

    fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val calendar = java.util.Calendar.getInstance()
        calendar.clear()
        calendar.set(java.util.Calendar.YEAR, year)
        calendar.set(java.util.Calendar.MONTH, month)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyTransactions: Flow<List<Transaction>> = _currentYearMonth.flatMapLatest { (year, month) ->
        val range = getMonthRange(year, month)
        transactionDao.getTransactionsByMonth(range.first, range.second)
    }

    val monthlyCategorySpending: Flow<List<CategorySpending>> = combine(
        categoryDao.getAllFlow(),
        monthlyTransactions
    ) { categories, transactions ->
        categories.map { category ->
            val total = transactions
                .filter { it.categoryId == category.id }
                .sumOf { it.amount }
            CategorySpending(category, total)
        }.sortedByDescending { it.totalAmount }
    }

    val monthlyTotals: Flow<MonthlyTotals> = monthlyTransactions.map { transactions ->
        val spent = transactions.sumOf { it.amount }
        val youOwe = transactions
            .filter { it.direction == TransactionDirection.I_OWE }
            .sumOf { it.amount }
        val owedToYou = transactions
            .filter { it.direction == TransactionDirection.THEY_OWE }
            .sumOf { it.amount }
        MonthlyTotals(spent, youOwe, owedToYou)
    }

    val monthlyDetailedTransactions: Flow<List<DetailedTransaction>> = combine(
        monthlyTransactions,
        tabDao.getAll(),
        friendDao.getAll(),
        categoryDao.getAllFlow()
    ) { transactions, tabs, friends, categories ->
        transactions.map { tx ->
            val tab = tabs.find { it.id == tx.tabId }
            val friend = tab?.let { t -> friends.find { it.id == t.friendId } }
            val category = categories.find { it.id == tx.categoryId }
            DetailedTransaction(
                transaction = tx,
                friendName = friend?.name ?: "Unknown",
                category = category
            )
        }.sortedByDescending { it.transaction.createdAt }
    }

    val splitBillsWithParticipantCount: Flow<List<SplitBillUiItem>> = combine(
        splitBillDao.getAllSplitBillsFlow(),
        splitBillDao.getAllParticipantsFlow()
    ) { bills, participants ->
        bills.map { bill ->
            val billParticipants = participants.filter { it.splitBillId == bill.id }
            val count = billParticipants.size
            val isAllPaid = billParticipants.isNotEmpty() && billParticipants.all { it.paymentStatus == PaymentStatus.PAID }
            val status = if (isAllPaid) "Settled" else "Pending"
            SplitBillUiItem(bill, count, status)
        }
    }

    fun getSplitBillDetailFlow(splitBillId: Long): Flow<SplitBillDetail?> {
        return combine(
            splitBillDao.getAllSplitBillsFlow(),
            splitBillDao.getAllParticipantsFlow(),
            friendDao.getAll(),
            categoryDao.getAllFlow()
        ) { bills, participants, friends, categories ->
            val bill = bills.find { it.id == splitBillId } ?: return@combine null
            val billParticipants = participants.filter { it.splitBillId == splitBillId }
            val participantDetails = billParticipants.map { p ->
                val friend = friends.find { it.id == p.friendId }
                ParticipantDetail(p, friend?.name ?: "Unknown")
            }
            val category = categories.find { it.id == bill.categoryId }
            SplitBillDetail(bill, category, participantDetails)
        }
    }

    fun toggleParticipantPaymentStatus(participant: SplitBillParticipant) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val newStatus = if (participant.paymentStatus == PaymentStatus.PAID) {
                    PaymentStatus.PENDING
                } else {
                    PaymentStatus.PAID
                }
                splitBillDao.updateParticipant(participant.copy(paymentStatus = newStatus))
            }
        }
    }

    fun addFriendDirect(name: String, upiVpa: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val friendId = withContext(Dispatchers.IO) {
                val friend = Friend(
                    name = name,
                    upiVpa = upiVpa,
                    createdAt = System.currentTimeMillis()
                )
                friendDao.insert(friend)
            }
            onComplete(friendId)
        }
    }

    fun splitBill(
        context: Context,
        totalAmount: Double,
        description: String,
        categoryId: Long?,
        friendShares: Map<Long, Double>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val timestamp = System.currentTimeMillis()
                val splitBill = SplitBill(
                    description = description,
                    totalAmount = totalAmount,
                    categoryId = categoryId,
                    createdAt = timestamp
                )
                val splitBillId = splitBillDao.insertSplitBill(splitBill)

                val participants = friendShares.map { (friendId, share) ->
                    SplitBillParticipant(
                        splitBillId = splitBillId,
                        friendId = friendId,
                        shareAmount = share,
                        paymentStatus = PaymentStatus.PENDING
                    )
                }
                splitBillDao.insertParticipants(participants)
            }
            onComplete()
        }
    }

    val categories: Flow<List<Category>> = categoryDao.getAllFlow()
    val friends: Flow<List<Friend>> = friendDao.getAll()

    val friendsWithoutOpenTab: Flow<List<Friend>> = combine(
        friendDao.getAll(),
        tabDao.getAllOpenTabs()
    ) { friendsList, openTabs ->
        val openFriendIds = openTabs.map { it.friendId }.toSet()
        friendsList.filter { it.id !in openFriendIds }
    }

    val categorySpending: Flow<List<CategorySpending>> = combine(
        categoryDao.getAllFlow(),
        transactionDao.getAll()
    ) { categories, transactions ->
        categories.map { category ->
            val total = transactions
                .filter { it.categoryId == category.id }
                .sumOf { it.amount }
            CategorySpending(category, total)
        }.sortedByDescending { it.totalAmount }
    }

    val openTabsWithFriend: Flow<List<TabWithFriend>> = combine(
        tabDao.getAllOpenTabs(),
        friendDao.getAll()
    ) { tabs, friends ->
        tabs.map { tab ->
            val friend = friends.find { it.id == tab.friendId }
            TabWithFriend(
                tab = tab,
                friendName = friend?.name ?: "Unknown"
            )
        }
    }

    fun addFriendAndTab(context: Context, name: String, upiVpa: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val timestamp = System.currentTimeMillis()
                val friend = Friend(
                    name = name,
                    upiVpa = upiVpa,
                    createdAt = timestamp
                )
                val friendId = friendDao.insert(friend)
                val tab = Tab(
                    friendId = friendId,
                    createdAt = timestamp,
                    status = TabStatus.OPEN,
                    totalOwedByMe = 0.0,
                    totalOwedToMe = 0.0
                )
                val tabId = tabDao.insert(tab)
                
                showNewTabNotification(context, tabId, friend.name)
            }
            onComplete()
        }
    }

    fun addFriendAndTabDirect(context: Context, name: String, upiVpa: String, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val friendId = withContext(Dispatchers.IO) {
                val timestamp = System.currentTimeMillis()
                val friend = Friend(
                    name = name,
                    upiVpa = upiVpa,
                    createdAt = timestamp
                )
                val fId = friendDao.insert(friend)
                val tab = Tab(
                    friendId = fId,
                    createdAt = timestamp,
                    status = TabStatus.OPEN,
                    totalOwedByMe = 0.0,
                    totalOwedToMe = 0.0
                )
                val tabId = tabDao.insert(tab)
                
                showNewTabNotification(context, tabId, friend.name)
                fId
            }
            onComplete(friendId)
        }
    }

    fun openTabForExistingFriend(context: Context, friendId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val timestamp = System.currentTimeMillis()
                val tab = Tab(
                    friendId = friendId,
                    createdAt = timestamp,
                    status = TabStatus.OPEN,
                    totalOwedByMe = 0.0,
                    totalOwedToMe = 0.0
                )
                val tabId = tabDao.insert(tab)
                val friendObj = friendDao.getById(friendId)
                friendObj?.let {
                    showNewTabNotification(context, tabId, it.name)
                }
            }
            onComplete()
        }
    }

    private fun showNewTabNotification(context: Context, tabId: Long, friendName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tabId", tabId)
            putExtra("friendName", friendName)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            tabId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "new_tabs")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New tab opened")
            .setContentText("You started a tab with $friendName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(tabId.toInt(), notification)
    }

    fun getTransactionsForTab(tabId: Long): Flow<List<Transaction>> {
        return transactionDao.getTransactionsByTabId(tabId)
    }

    fun getTabFlow(tabId: Long): Flow<Tab?> {
        return tabDao.getTabByIdFlow(tabId)
    }

    private suspend fun recalculateTabBalance(tabId: Long) {
        val tab = tabDao.getById(tabId) ?: return
        val txs = transactionDao.getTransactionsByTabIdDirect(tabId)
        val unpaid = txs.filter { it.paymentStatus != PaymentStatus.PAID }
        val totalToMe = unpaid.filter { it.direction == TransactionDirection.I_OWE }.sumOf { it.amount }
        val totalByMe = unpaid.filter { it.direction == TransactionDirection.THEY_OWE }.sumOf { it.amount }
        val updatedTab = tab.copy(
            totalOwedToMe = totalToMe,
            totalOwedByMe = totalByMe
        )
        tabDao.update(updatedTab)
    }

    fun addTransaction(
        tabId: Long,
        amount: Double,
        description: String,
        direction: TransactionDirection,
        categoryId: Long?
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val transaction = Transaction(
                    tabId = tabId,
                    amount = amount,
                    description = description,
                    createdAt = System.currentTimeMillis(),
                    direction = direction,
                    paymentStatus = PaymentStatus.PENDING,
                    categoryId = categoryId
                )
                transactionDao.insert(transaction)
                recalculateTabBalance(tabId)
            }
        }
    }

    fun deleteTransactions(tabId: Long, transactionIds: List<Long>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                transactionDao.deleteTransactions(transactionIds)
                recalculateTabBalance(tabId)
            }
        }
    }

    fun updateTransaction(
        transactionId: Long,
        newAmount: Double,
        newDescription: String,
        newDirection: TransactionDirection,
        newCategoryId: Long?,
        newPaymentStatus: PaymentStatus
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val oldTransaction = transactionDao.getById(transactionId) ?: return@withContext
                val updatedTransaction = oldTransaction.copy(
                    amount = newAmount,
                    description = newDescription,
                    direction = newDirection,
                    categoryId = newCategoryId,
                    paymentStatus = newPaymentStatus
                )
                transactionDao.update(updatedTransaction)
                recalculateTabBalance(oldTransaction.tabId)
            }
        }
    }

    fun updateFriend(friendId: Long, newName: String, newUpiVpa: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val oldFriend = friendDao.getById(friendId) ?: return@withContext
                val updatedFriend = oldFriend.copy(
                    name = newName,
                    upiVpa = newUpiVpa
                )
                friendDao.update(updatedFriend)
            }
        }
    }


    fun deleteTab(tabId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val tab = tabDao.getById(tabId)
                if (tab != null) {
                    tabDao.delete(tab)
                }
            }
        }
    }

    fun deleteSplitBill(splitBillId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                splitBillDao.deleteSplitBillById(splitBillId)
            }
        }
    }
}

class TabSplitterViewModelFactory(
    private val friendDao: FriendDao,
    private val tabDao: TabDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val splitBillDao: SplitBillDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TabSplitterViewModel::class.java)) {
            return TabSplitterViewModel(friendDao, tabDao, transactionDao, categoryDao, splitBillDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
