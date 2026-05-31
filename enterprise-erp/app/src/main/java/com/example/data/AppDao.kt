package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Inventory Items
    @Query("SELECT * FROM inventory_items ORDER BY lastUpdated DESC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItemById(id: Int)

    // Finance Transactions
    @Query("SELECT * FROM finance_transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransaction)

    @Update
    suspend fun updateTransaction(transaction: FinanceTransaction)

    // Employees
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Int)

    // Approval Requests
    @Query("SELECT * FROM approval_requests ORDER BY timestamp DESC")
    fun getAllApprovalRequests(): Flow<List<ApprovalRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalRequest(request: ApprovalRequest)

    @Update
    suspend fun updateApprovalRequest(request: ApprovalRequest)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    // User Accounts Security DAO
    @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
    suspend fun getUserAccount(username: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccount)

    @Query("SELECT * FROM user_accounts ORDER BY username ASC")
    fun getAllUserAccounts(): Flow<List<UserAccount>>

    // --- NEW DAOS FOR COMPREHENSIVE FEATURES ---

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // DMS Documents
    @Query("SELECT * FROM dms_documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<DmsDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DmsDocument)

    @Query("DELETE FROM dms_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Int)

    // Manufacturing Orders
    @Query("SELECT * FROM manufacturing_orders ORDER BY timestamp DESC")
    fun getAllManufacturingOrders(): Flow<List<ManufacturingOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManufacturingOrder(order: ManufacturingOrder)

    @Update
    suspend fun updateManufacturingOrder(order: ManufacturingOrder)

    // Chart of Accounts
    @Query("SELECT * FROM chart_of_accounts ORDER BY code ASC")
    fun getAllChartOfAccounts(): Flow<List<ChartOfAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChartOfAccount(account: ChartOfAccount)

    @Query("UPDATE chart_of_accounts SET balance = balance + :amount WHERE code = :code")
    suspend fun updateAccountBalance(code: String, amount: Double)

    // Journal Entries
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)
}
