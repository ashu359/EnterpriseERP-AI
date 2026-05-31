package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Database(
    entities = [
        InventoryItem::class,
        FinanceTransaction::class,
        Employee::class,
        ApprovalRequest::class,
        AuditLog::class,
        ChatMessage::class,
        UserAccount::class,
        NotificationEntity::class,
        DmsDocument::class,
        ManufacturingOrder::class,
        ChartOfAccount::class,
        JournalEntry::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "enterprise_erp_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            // Seed Employees with Performance KPIs
            dao.insertEmployee(Employee(1, "Alice Vance", "alice.vance@enterprise.io", "Management", "ADMIN", 125000.0, false, null, 98f, 95f, 92f, 4.8f))
            dao.insertEmployee(Employee(2, "Sarah Connor", "sarah.connor@enterprise.io", "Finance", "MANAGER", 95000.0, true, System.currentTimeMillis() - 3600000 * 4, 94f, 88f, 80f, 4.4f))
            dao.insertEmployee(Employee(3, "John Doe", "john.doe@enterprise.io", "Inventory", "EMPLOYEE", 62000.0, true, System.currentTimeMillis() - 3600000 * 2, 99f, 96f, 85f, 4.2f))
            dao.insertEmployee(Employee(4, "Ethan Hunt", "ethan.hunt@enterprise.io", "Security", "MANAGER", 105000.0, false, null, 97f, 99f, 98f, 4.9f))
            dao.insertEmployee(Employee(5, "Vikram Malhotra", "vikram.m@enterprise.io", "DMS Operations", "EMPLOYEE", 58000.0, true, System.currentTimeMillis() - 3600000, 91f, 90f, 88f, 4.1f))

            // Seed User Accounts
            dao.insertUserAccount(UserAccount("admin", "admin", "Alice Vance", "ADMIN", "alice.vance@enterprise.io", "Management", System.currentTimeMillis(), true, "123456"))
            dao.insertUserAccount(UserAccount("manager", "manager", "Sarah Connor", "MANAGER", "sarah.connor@enterprise.io", "Finance", System.currentTimeMillis(), false, "123456"))
            dao.insertUserAccount(UserAccount("employee", "employee", "John Doe", "EMPLOYEE", "john.doe@enterprise.io", "Inventory", System.currentTimeMillis(), false, "123456"))

            // Seed Inventory items for multiple warehouses
            dao.insertInventoryItem(InventoryItem(1, "Kraft Paper Rolls", "SKU0001", 120, 50, 45.0, "Raw Materials", System.currentTimeMillis(), "Pune Warehouse"))
            dao.insertInventoryItem(InventoryItem(2, "Electronics Microchips", "SKU0002", 15, 30, 250.0, "Raw Materials", System.currentTimeMillis(), "Nagpur Warehouse"))
            dao.insertInventoryItem(InventoryItem(3, "Industrial Screws M6", "SKU0003", 2500, 500, 0.15, "Raw Materials", System.currentTimeMillis(), "Mumbai Warehouse"))
            dao.insertInventoryItem(InventoryItem(4, "Cardboard Boxes Large", "SKU0004", 400, 100, 2.5, "Office Supplies", System.currentTimeMillis(), "Pune Warehouse"))
            dao.insertInventoryItem(InventoryItem(5, "Solar Panel Inverters 5KW", "SKU0005", 28, 10, 850.0, "Finished Goods", System.currentTimeMillis(), "Pune Warehouse"))
            dao.insertInventoryItem(InventoryItem(6, "Li-Ion Battery Cell 3.7V", "SKU0006", 1420, 300, 4.25, "Raw Materials", System.currentTimeMillis(), "Nagpur Warehouse"))

            // Seed Finance Transactions
            dao.insertTransaction(FinanceTransaction(1, "REVENUE", 15000.0, "Electronics Sales", "Batch sales of high-performance components", System.currentTimeMillis() - 86400000 * 2, "COMPLETED", "Alice Vance"))
            dao.insertTransaction(FinanceTransaction(2, "EXPENSE", 4500.0, "Payroll", "Salary disbursements for contractor engineering staff", System.currentTimeMillis() - 86400000, "COMPLETED", "Sarah Connor"))
            dao.insertTransaction(FinanceTransaction(3, "REVENUE", 8500.0, "Consulting", "Enterprise integration design services billed", System.currentTimeMillis(), "COMPLETED", "Alice Vance"))
            dao.insertTransaction(FinanceTransaction(4, "EXPENSE", 3200.0, "SaaS Infrastructure", "Amazon AWS Cloud Hosting monthly compute & database services", System.currentTimeMillis() - 86400000 * 3, "COMPLETED", "Alice Vance"))
            dao.insertTransaction(FinanceTransaction(5, "REVENUE", 24500.0, "SaaS Subscriptions", "Enterprise tenant global monthly subscription licenses", System.currentTimeMillis() - 86400000 * 5, "COMPLETED", "Sarah Connor"))

            // Seed Multi-Stage Approval Requests
            dao.insertApprovalRequest(ApprovalRequest(
                id = 1,
                title = "Purchase Requisition: Microchip Assembly Units",
                requestType = "INVENTORY",
                details = "Acquisition of 200 units of SKU0002. Total value: $50,000.",
                amountOrQuantity = 200.0,
                requester = "John Doe",
                status = "PENDING",
                timestamp = System.currentTimeMillis() - 86400000 * 3, // 3 days old -> will trigger escalation!
                currentStage = "FINANCE Approval",
                comments = "Manager approved. Routing to Finance level.",
                historyLog = "Created by John Doe\nApproved by Manager Sarah Connor"
            ))

            dao.insertApprovalRequest(ApprovalRequest(
                id = 2,
                title = "Expense Authorization: Annual Software Licenses",
                requestType = "FINANCE",
                details = "Enterprise Room and Gemini API server workspace licenses renewing.",
                amountOrQuantity = 1200.0,
                requester = "Sarah Connor",
                status = "PENDING",
                timestamp = System.currentTimeMillis(),
                currentStage = "MANAGER Approval",
                comments = null,
                historyLog = "Created by Sarah Connor"
            ))

            dao.insertApprovalRequest(ApprovalRequest(
                id = 3,
                title = "Sourcing Contract Approval: SolarTech Allied",
                requestType = "PROCUREMENT",
                details = "B2B procurement agreement for eco-friendly solar components. Value: $12,500.",
                amountOrQuantity = 12500.0,
                requester = "Alice Vance",
                status = "APPROVED",
                timestamp = System.currentTimeMillis() - 86400000,
                currentStage = "COMPLETED",
                comments = "Compliance guidelines fully checked. Verified by Ethan Hunt.",
                historyLog = "Created by Alice Vance\nApproved by Manager Ethan Hunt"
            ))

            // Seed Notifications History
            dao.insertNotification(NotificationEntity(1, "EMAIL", "Security Notice", "Successful login detected for user admin on device Pixel 8 Pro.", "Leave Approved", System.currentTimeMillis() - 3600000 * 2, false))
            dao.insertNotification(NotificationEntity(2, "PUSH", "Invoice Alert", "Supplier Invoice #2039 is overdue.", "Invoice Overdue", System.currentTimeMillis() - 3600000 * 5, true))
            dao.insertNotification(NotificationEntity(3, "WHATSAPP", "Leave Scheduled", "Your vacation leave starting June 10 is approved.", "Leave Approved", System.currentTimeMillis(), false))
            dao.insertNotification(NotificationEntity(4, "IN_APP", "System Broadcast", "Upcoming birthday of engineer Alice Vance tomorrow!", "Employee Birthday", System.currentTimeMillis(), false))

            // Seed DMS Documents with Reminders and Version Control
            dao.insertDocument(DmsDocument(1, "PAN_Card_Alice_Corp.pdf", "PAN", 1, "/docs/pan_alice.pdf", "N/A", System.currentTimeMillis() - 86400000 * 10))
            dao.insertDocument(DmsDocument(2, "Lease_Contract_Mumbai_HQ.pdf", "Contract", 2, "/docs/lease_mumbai.pdf", "2026-12-31", System.currentTimeMillis() - 86400000 * 5))
            dao.insertDocument(DmsDocument(3, "Supplier_Invoice_2041_Audit.pdf", "Invoice", 1, "/docs/invoice_2041.pdf", "2026-06-15", System.currentTimeMillis()))
            dao.insertDocument(DmsDocument(4, "Board_Resolution_May2026.pdf", "Corporate", 1, "/docs/corp_res_2026_05.pdf", "N/A", System.currentTimeMillis() - 3600000 * 4))

            // Seed Manufacturing Orders
            dao.insertManufacturingOrder(ManufacturingOrder(1, "Circuit Board Assembly", 50, "BOM: 50 Microchips, 200 Screws", "Acoustic Workstation B", "Assembly Mech v3", "PRODUCTION_ORDER", "PENDING", System.currentTimeMillis() - 3600000 * 3))
            dao.insertManufacturingOrder(ManufacturingOrder(2, "Wrapped Kraft Paper Roll Box", 100, "BOM: 100 Paper Rolls, 100 Boxes", "Wrapping Line C", "Packer Pro A", "QUALITY_CHECK", "APPROVED", System.currentTimeMillis() - 3600000))
            dao.insertManufacturingOrder(ManufacturingOrder(3, "Power Inverter Calibration", 20, "BOM: 20 Inverter Units, 40 Cables", "Calibration Lab D", "Calibrator System Z", "PRODUCTION_ORDER", "PENDING", System.currentTimeMillis() - 7200000))

            // Seed Chart of Accounts
            dao.insertChartOfAccount(ChartOfAccount("1010", "Cash & Cash Equivalents", "ASSET", 125400.00))
            dao.insertChartOfAccount(ChartOfAccount("1120", "Accounts Receivable (AR)", "ASSET", 45000.00))
            dao.insertChartOfAccount(ChartOfAccount("1200", "Inventory Asset", "ASSET", 35000.00))
            dao.insertChartOfAccount(ChartOfAccount("1220", "Warehouse Machinery & Hardware", "ASSET", 92000.00))
            dao.insertChartOfAccount(ChartOfAccount("2010", "Accounts Payable (AP)", "LIABILITY", 12500.00))
            dao.insertChartOfAccount(ChartOfAccount("2110", "Accrued Employee Payroll Liabilities", "LIABILITY", 4500.00))
            dao.insertChartOfAccount(ChartOfAccount("3010", "Partner Equity", "EQUITY", 100000.00))
            dao.insertChartOfAccount(ChartOfAccount("4010", "Corporate Sales Revenue", "INCOME", 165000.00))
            dao.insertChartOfAccount(ChartOfAccount("4020", "SaaS Support & Retainer Fees", "INCOME", 32000.00))
            dao.insertChartOfAccount(ChartOfAccount("5010", "Payroll & Compensation", "EXPENSE", 72100.00))

            // Seed Journal Entries
            dao.insertJournalEntry(JournalEntry(1, "Discharge contractor salaries", "5010", "1010", 4500.00, System.currentTimeMillis() - 86400000))
            dao.insertJournalEntry(JournalEntry(2, "Post item sales collections", "1010", "4010", 15000.00, System.currentTimeMillis() - 86400000 * 2))
            dao.insertJournalEntry(JournalEntry(3, "Paid Amazon Web Services cloud server rentals", "5010", "1010", 3200.00, System.currentTimeMillis() - 86400000 * 3))

            // Seed Audit Logs
            dao.insertAuditLog(AuditLog(1, System.currentTimeMillis() - 86400000 * 5, "Database System Initialized", "SYSTEM", "SYSTEM", "ALL", "SYSTEM", 0, "N/A", "Active", "127.0.0.1", "Mainframe Server", "Enterprise Core Cloud"))
            dao.insertAuditLog(AuditLog(2, System.currentTimeMillis() - 86400000 * 4, "Created Default Roles & Workspace Mapping", "Alice Vance", "ADMIN", "ALL", "UserAccount", 1, "N/A", "Security Active", "192.168.1.100", "Desktop Workstation", "Pune Admin Branch"))
        }
    }
}
