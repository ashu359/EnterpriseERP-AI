# Enterprise ERP System (Android)

An offline-first, enterprise-grade Android application engineered with Jetpack Compose, Kotlin, and continuous SQLite local tracking. This system provides standard organizational management, incorporating inventory, finance double ledger accounting, human resources, AI bots, workflow approvals, and strict audit logs.

## 🚀 Features

- **Sleek jetpack Compose UI** - Custom, professional dark-mode default setup utilizing Material Design 3 (M3).
- **Multitenant Role Access Controls (RBAC)** - Interactive Admin, Manager, and Employee gates controlling permissions and critical modules dynamically.
- **Double-Entry Ledger Accounting Engine** - Interactive Chart of Accounts system, Balance Sheet calculations, Cash Reserves tracking, and Journal entry journals.
- **Advanced Inventory & Multi-Warehouse segmenting** - Dynamic stock valuation, low-stock warnings, specific physical segment catalogs (Pune, Nagpur, Mumbai, etc.).
- **Escalated Multi-Stage Approvals** - Procurement, Finance, and Security request workflows with full history tracking.
- **Recruitment stage tracking** - Dynamic HR candidates tracker running stages from applied states to offer letter and hired status.
- **Manufacturing Execution System (MES)** - Bill of Materials (BOM) trackers, workstation assignments, and quality check states.
- **Document Management System (DMS)** - PDF scanner record simulations, renewal notifications, and invoice metadata indexes.
- **AI-Powered Bot Companion** - Native Gemini AI chat support integrated directly with conversational message caches using your AI Studio API keys.
- **Interactive SQL Database terminal** - Database console allowing operators to write and execute raw SELECT, PRAGMA, or EXPLAIN queries securely against the Room Database.
- **Escrow Trial Trail / Immutable Audit Logs** - Detailed logging system tracking columns changed, specific table modifications, operators, terminal IP addresses, and previous vs. future value shifts.

## 🛠️ Secure Setup & Testing

### 1. API Key Configuration
This application integrates with **Gemini models** securely. Do NOT hardcode API credentials:
- Open the **Secrets panel in Google AI Studio**.
- Create a secret named `GEMINI_API_KEY` and insert your personal API key.
- Android uses the Secrets Gradle plugin to inject this context safely into the JVM build parameters at compilation: `BuildConfig.GEMINI_API_KEY`.

### 2. Available Build Task Scripts
Use Gradle tasks to clean, compile, build and test:

* Build standard debug APK:
  ```bash
  gradle assembleDebug
  ```
* Run test suites:
  ```bash
  gradle :app:testDebugUnitTest
  ```
* Check code style rules:
  ```bash
  gradle lint
  ```

## 📁 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt        # Root Activity, Edge-to-Edge, & Crash safety
│   │   │   ├── data/                  # Persistent SQLite Data Layer (Room Engine)
│   │   │   │   ├── AppDao.kt          # Database DAO interface mapping entities
│   │   │   │   ├── AppDatabase.kt     # Main database instance containing record seeding
│   │   │   │   ├── Entities.kt        # Data Schema models (Staff, Inventory, Documents, etc.)
│   │   │   │   ├── GeminiService.kt   # Native Gemini API request handler (OkHttpClient)
│   │   │   │   └── Repository.kt      # Single source of truth repository coordinator
│   │   │   └── ui/                    # UI Components and Screen Layouts
│   │   │       ├── ERPScreens.kt      # Material 3 Modular screens, Navigation & Controls
│   │   │       ├── ERPViewModel.kt    # Screen ViewModels, state machines & SQL engine
│   │   │       └── theme/             # Global Theme System (Color scheme, Typography, Shapes)
```

## 🧩 Modifying Modules & Page Editing

* **Add Columns/Database Entities**: Edit `app/src/main/java/com/example/data/Entities.kt` and register them in `AppDatabase.kt`.
* **Theme Styling & Colors**: Access `app/src/main/java/com/example/ui/theme/Color.kt` and `Theme.kt` to update the sleek slate palette color parameters.
* **Customize ERP layouts**: Edit `app/src/main/java/com/example/ui/ERPScreens.kt` blocks to customize individual screens (e.g. `DashboardModule`, `FinanceModule`, `AuditLogsModule`).

## 🎨 Component Usages & Accessibility

This system strictly matches Material 3 density recommendations:
- **Component targets**: Ensuring touch size target inputs are minimum `48dp` x `48dp`.
- **Dynamic Icons**: High-contrast helper indicators mapping clear status colors (alert reds, approved greens).
- **Edge-to-Edge**: Full screen layouts, handling System Insets margins transparently using compose constraints.
