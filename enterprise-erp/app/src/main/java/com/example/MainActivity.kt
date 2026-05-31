package com.example

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.ERPSystemMainView
import com.example.ui.ERPViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Diagnostic global crash handler to capture exact crash details
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      Log.e("CRASH_LOGGER", "CRASH DETECTED on thread ${thread.name}", throwable)
      throwable.printStackTrace()
      defaultHandler?.uncaughtException(thread, throwable)
    }

    enableEdgeToEdge()
    
    var startupError: Throwable? = null
    var viewModel: ERPViewModel? = null
    
    try {
        // Obtain the ViewModel stably using AndroidViewModelFactory to ensure JVM constructor is invoked with Application
        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
        )[ERPViewModel::class.java]
    } catch (e: Throwable) {
        Log.e("CRASH_LOGGER", "Failed to initialize ERPViewModel", e)
        startupError = e
    }
    
    setContent {
      var runtimeError by remember { mutableStateOf(startupError) }
      
      if (runtimeError != null) {
          Box(
              modifier = Modifier
                  .fillMaxSize()
                  .background(Color(0xFF1E1E2C))
                  .padding(24.dp),
              contentAlignment = Alignment.Center
          ) {
              Column(
                  modifier = Modifier
                      .fillMaxWidth()
                      .verticalScroll(rememberScrollState()),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                  Text(
                      text = "Startup Initialization Error",
                      color = Color(0xFFFF5252),
                      fontSize = 22.sp,
                      fontFamily = FontFamily.SansSerif
                  )
                  
                  Text(
                      text = "An exception occurred while initializing the database or viewModel. You can reset the database schema to repair the application.",
                      color = Color.White,
                      fontSize = 14.sp
                  )
                  
                  Card(
                      colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D3E)),
                      modifier = Modifier.fillMaxWidth()
                  ) {
                      Text(
                          text = runtimeError?.stackTraceToString() ?: "Unknown initialization error",
                          fontFamily = FontFamily.Monospace,
                          fontSize = 11.sp,
                          color = Color(0xFFE0E0E0),
                          modifier = Modifier.padding(12.dp)
                      )
                  }
                  
                  Row(
                      horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                      Button(
                          onClick = {
                              // Perform database reset in background and restart MainActivity
                              GlobalScope.launch(Dispatchers.IO) {
                                  try {
                                      val context = applicationContext
                                      context.deleteDatabase("enterprise_erp_database")
                                      Log.i("CRASH_LOGGER", "Database dropped successfully. Restarting activity.")
                                      launch(Dispatchers.Main) {
                                          val intent = intent
                                          finish()
                                          startActivity(intent)
                                      }
                                  } catch (ex: Throwable) {
                                      Log.e("CRASH_LOGGER", "Failed to clear database", ex)
                                  }
                              }
                          },
                          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                      ) {
                          Text("Reset Database & Open ERP", color = Color.White)
                      }
                      
                      Button(
                          onClick = {
                              val intent = intent
                              finish()
                              startActivity(intent)
                          },
                          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                      ) {
                          Text("Retry Initialization", color = Color.White)
                      }
                  }
              }
          }
      } else {
          val vm = viewModel
          if (vm != null) {
              ERPSystemMainView(viewModel = vm)
          } else {
              Box(
                  modifier = Modifier.fillMaxSize().background(Color(0xFF1E1E2C)),
                  contentAlignment = Alignment.Center
              ) {
                  CircularProgressIndicator()
              }
          }
      }
    }
  }
}
