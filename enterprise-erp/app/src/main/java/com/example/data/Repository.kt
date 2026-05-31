package com.example.data

import kotlinx.coroutines.flow.Flow

class Repository(private val appDao: AppDao) {

    // Flows for Reactive UI observing
    val allInventoryItems: Flow<List<InventoryItem>> = appDao.getAllInventoryItems()
    val allTransactions: Flow<List<FinanceTransaction>> = appDao.getAllTransactions()
    val allEmployees: Flow<List<Employee>> = appDao.getAllEmployees()
    val allApprovalRequests: Flow<List<ApprovalRequest>> = appDao.getAllApprovalRequests()
    val allAuditLogs: Flow<List<AuditLog>> = appDao.getAllAuditLogs()
    val allChatMessages: Flow<List<ChatMessage>> = appDao.getAllChatMessages()

    // Inventory operations
    suspend fun insertInventoryItem(item: InventoryItem) {
        appDao.insertInventoryItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) {
        appDao.updateInventoryItem(item)
    }

    suspend fun deleteInventoryItemById(id: Int) {
        appDao.deleteInventoryItemById(id)
    }

    // Finance operations
    suspend fun insertTransaction(transaction: FinanceTransaction) {
        appDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: FinanceTransaction) {
        appDao.updateTransaction(transaction)
    }

    // Employee operations
    suspend fun insertEmployee(employee: Employee) {
        appDao.insertEmployee(employee)
    }

    suspend fun updateEmployee(employee: Employee) {
        appDao.updateEmployee(employee)
    }

    suspend fun deleteEmployeeById(id: Int) {
        appDao.deleteEmployeeById(id)
    }

    // Approval Request operations
    suspend fun insertApprovalRequest(request: ApprovalRequest) {
        appDao.insertApprovalRequest(request)
    }

    suspend fun updateApprovalRequest(request: ApprovalRequest) {
        appDao.updateApprovalRequest(request)
    }

    // Audit Log operations
    suspend fun insertAuditLog(log: AuditLog) {
        appDao.insertAuditLog(log)
    }

    // Chat operations
    suspend fun insertChatMessage(message: ChatMessage) {
        appDao.insertChatMessage(message)
    }

    suspend fun clearChatMessages() {
        appDao.clearChatMessages()
    }

    // User Accounts security operations
    val allUserAccounts: Flow<List<UserAccount>> = appDao.getAllUserAccounts()

    suspend fun insertUserAccount(user: UserAccount) {
        appDao.insertUserAccount(user)
    }

    suspend fun getUserAccount(username: String): UserAccount? {
        return appDao.getUserAccount(username)
    }

    // --- EXPANDED MODULE OPERATIONS FOR NEW CAPABILITIES ---

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = appDao.getAllNotifications()

    suspend fun insertNotification(notification: NotificationEntity) {
        appDao.insertNotification(notification)
    }

    suspend fun markNotificationAsRead(id: Int) {
        appDao.markNotificationAsRead(id)
    }

    suspend fun clearAllNotifications() {
        appDao.clearAllNotifications()
    }

    // DMS Documents
    val allDocuments: Flow<List<DmsDocument>> = appDao.getAllDocuments()

    suspend fun insertDocument(document: DmsDocument) {
        appDao.insertDocument(document)
    }

    suspend fun deleteDocumentById(id: Int) {
        appDao.deleteDocumentById(id)
    }

    // Manufacturing Orders
    val allManufacturingOrders: Flow<List<ManufacturingOrder>> = appDao.getAllManufacturingOrders()

    suspend fun insertManufacturingOrder(order: ManufacturingOrder) {
        appDao.insertManufacturingOrder(order)
    }

    suspend fun updateManufacturingOrder(order: ManufacturingOrder) {
        appDao.updateManufacturingOrder(order)
    }

    // Chart of Accounts
    val allChartOfAccounts: Flow<List<ChartOfAccount>> = appDao.getAllChartOfAccounts()

    suspend fun insertChartOfAccount(account: ChartOfAccount) {
        appDao.insertChartOfAccount(account)
    }

    suspend fun updateAccountBalance(code: String, amount: Double) {
        appDao.updateAccountBalance(code, amount)
    }

    // Journal Entries
    val allJournalEntries: Flow<List<JournalEntry>> = appDao.getAllJournalEntries()

    suspend fun insertJournalEntry(entry: JournalEntry) {
        appDao.insertJournalEntry(entry)
    }
}
