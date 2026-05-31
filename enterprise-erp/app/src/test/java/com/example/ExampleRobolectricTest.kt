package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.ERPSystemMainView
import com.example.ui.ERPViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun testFullAppStartup() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ERPViewModel(application)
    composeTestRule.setContent {
      ERPSystemMainView(viewModel = viewModel)
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun testSQLConsoleDialogRendering() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ERPViewModel(application)
    composeTestRule.setContent {
      com.example.ui.SQLConsoleDialog(
        viewModel = viewModel,
        onDismiss = {}
      )
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun testExecuteSQLSelectStatement() = kotlinx.coroutines.runBlocking {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ERPViewModel(application)
    viewModel.executeRawSQL("SELECT * FROM inventory_items")
    // Wait a brief moment for the coroutine to complete
    repeat(10) {
      if (viewModel.sqlQueryResult.value != null) return@runBlocking
      kotlinx.coroutines.delay(100)
    }
    val result = viewModel.sqlQueryResult.value
    org.junit.Assert.assertNotNull("SQL Result should not be null", result)
    org.junit.Assert.assertNull("SQL Error should be null: " + result?.errorMessage, result?.errorMessage)
    org.junit.Assert.assertTrue("Row count should be greater than 0", (result?.rows?.size ?: 0) > 0)
  }

  @Test
  fun testLoadAppIconDrawable() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_launcher_background)
    val foreground = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
    org.junit.Assert.assertNotNull(background)
    org.junit.Assert.assertNotNull(foreground)
  }
}
