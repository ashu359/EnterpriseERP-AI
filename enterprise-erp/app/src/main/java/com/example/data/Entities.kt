package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sku: String,
    val quantity: Int,
    val reorderPoint: Int,
    val price: Double,
    val category: String, // e.g., "Raw Materials", "Finished Goods", "Office Supplies"
    val lastUpdated: Long = System.currentTimeMillis(),
    val warehouse: String = "Pune Warehouse" // Nagpur, Pune, Mumbai
)

@Entity(tableName = "finance_transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "REVENUE" or "EXPENSE"
    val amount: Double,
    val category: String, // e.g., "Sales", "Payroll", "Inventory Purchases", "Software License"
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val status: String, // "COMPLETED" or "PENDING"
    val approvedBy: String? = null
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val department: String, // "Management", "Finance", "Inventory", "HR"
    val role: String, // "ADMIN", "MANAGER", "EMPLOYEE"
    val salary: Double,
    val isClockedIn: Boolean = false,
    val lastClockIn: Long? = null,
    // KPIs for Performance
    val attendanceScore: Float = 95f,
    val taskCompletion: Float = 90f,
    val salesTarget: Float = 85f,
    val customerFeedback: Float = 4.5f
)

@Entity(tableName = "approval_requests")
data class ApprovalRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val requestType: String, // "INVENTORY", "FINANCE", "HR"
    val details: String,
    val amountOrQuantity: Double, // e.g., price or items requested
    val requester: String,
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis(),
    // Expanded Approval Workflow properties
    val currentStage: String = "MANAGER Approval", // "MANAGER Approval" -> "FINANCE Approval" -> "DIRECTOR Approval" -> "PO Generated"
    val comments: String? = null,
    val historyLog: String = "Request created"
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val performedBy: String,
    val role: String,
    val module: String, // "INVENTORY", "FINANCE", "HR", "ALL", "MANUFACTURING", "ACCOUNTING", "SECURITY"
    // Expanded Audit properties
    val tableName: String = "SYSTEM",
    val recordId: Int = 0,
    val oldValue: String = "N/A",
    val newValue: String = "N/A",
    val ipAddress: String = "192.168.43.102",
    val device: String = "Pixel 8 Pro (Android 14)",
    val location: String = "Pune, MH, India"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val role: String, // "user" or "model"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String, // "ADMIN", "MANAGER", "EMPLOYEE"
    val email: String,
    val department: String,
    val createdTime: Long = System.currentTimeMillis(),
    val twoFactorEnabled: Boolean = false,
    val twoFactorSecret: String = "123456"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "EMAIL", "PUSH", "SMS", "WHATSAPP", "IN_APP"
    val title: String,
    val message: String,
    val category: String, // "Leave Approved", "PO Approved", "Invoice Overdue", "Birthday"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "dms_documents")
data class DmsDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // "Aadhaar", "PAN", "Offer Letter", "Contract", "Invoice"
    val version: Int = 1,
    val filePath: String,
    val expiryDate: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "manufacturing_orders")
data class ManufacturingOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val quantity: Int,
    val bomDetails: String,
    val workCenter: String,
    val machineUsed: String,
    val status: String = "RAW_MATERIAL", // "RAW_MATERIAL" -> "PRODUCTION_ORDER" -> "MANUFACTURING" -> "QUALITY_CHECK" -> "FINISHED_GOODS"
    val qualityStatus: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chart_of_accounts")
data class ChartOfAccount(
    @PrimaryKey val code: String, // e.g., "1010", "2010", etc.
    val name: String,
    val type: String, // "ASSET", "LIABILITY", "INCOME", "EXPENSE", "EQUITY"
    val balance: Double = 0.0
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val debitAccount: String,
    val creditAccount: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class SQLResult(
    val headers: List<String>,
    val rows: List<List<String>>,
    val affectedRows: Int = 0,
    val errorMessage: String? = null
)

