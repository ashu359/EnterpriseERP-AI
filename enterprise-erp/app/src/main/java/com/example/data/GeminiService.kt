package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(userMessage: String, chatHistory: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            return@withContext "Gemini API key is not configured yet. Please enter a valid GEMINI_API_KEY in the AI Studio Secrets panel."
        }

        val modelsToTry = listOf("gemini-3.5-flash", "gemini-2.5-flash")
        var lastErrorMessage = ""
        var lastErrorCode = 0

        for (model in modelsToTry) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            
            // Try up to 2 times for each model in case of transient 503/500 errors
            for (attempt in 1..2) {
                try {
                    val root = JSONObject()
                    val contentsArray = JSONArray()
                    
                    // Limit historical turns for brief context window
                    chatHistory.takeLast(8).forEach { msg ->
                        val turnObj = JSONObject()
                        turnObj.put("role", if (msg.role == "user") "user" else "model")
                        val partsArr = JSONArray()
                        val partObj = JSONObject()
                        partObj.put("text", msg.text)
                        partsArr.put(partObj)
                        turnObj.put("parts", partsArr)
                        contentsArray.put(turnObj)
                    }
                    
                    // Append current message
                    val currentTurn = JSONObject()
                    currentTurn.put("role", "user")
                    val partsArr = JSONArray()
                    val partObj = JSONObject()
                    partObj.put("text", userMessage)
                    partsArr.put(partObj)
                    currentTurn.put("parts", partsArr)
                    contentsArray.put(currentTurn)
                    
                    root.put("contents", contentsArray)

                    // Setup professional system boundaries
                    val systemInstructionObj = JSONObject()
                    val sysPartsArr = JSONArray()
                    val sysPartObj = JSONObject()
                    sysPartObj.put("text", """
                        You are Enterprise ERP AI, an intelligent co-pilot built within the Enterprise ERP application.
                        Your goal is to assist warehouse clerks, analysts, and directors about administrative operations.
                        
                        The Enterprise ERP modules:
                        1. Inventory Management: Shows items catalog, raw materials, office supplies, reorder thresholds (items below threshold trigger warning badges!).
                        2. Finance: Manages company credit and debit ledgers, revenue streams, invoices, and expense sheets.
                        3. Human Resources: Shows lists of personnel, departments, salaries, and real-time clock-in/out.
                        
                        Also, there's a manager approval center for processing urgent inventory procurement or leaf schedules.
                        
                        Respond in a concise, highly professional, supportive, and business-focused manner. Keep your response under 3 descriptive paragraphs. If the user asks to modify records, gently suggest they navigate to the corresponding module screen to execute changes securely.
                    """.trimIndent())
                    sysPartsArr.put(sysPartObj)
                    systemInstructionObj.put("parts", sysPartsArr)
                    root.put("systemInstruction", systemInstructionObj)

                    val jsonBody = root.toString()
                    val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
                    
                    val request = Request.Builder()
                        .url(url)
                        .post(requestBody)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: return@withContext "Error: Received empty response from model."
                            val responseJson = JSONObject(responseBody)
                            val candidates = responseJson.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val firstCandidate = candidates.getJSONObject(0)
                                val contentObj = firstCandidate.optJSONObject("content")
                                val parts = contentObj?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    return@withContext parts.getJSONObject(0).optString("text", "No readable text generated.")
                                }
                            }
                            return@withContext "Error decoding chatbot candidate content."
                        } else {
                            val errBody = response.body?.string() ?: ""
                            lastErrorCode = response.code
                            lastErrorMessage = errBody
                            Log.w(TAG, "API call failed for model ${model} (attempt ${attempt}) with code ${response.code}: $errBody")
                            
                            // If key is invalid (400) or unauthorized/expired (403), do not retry or try other models. Return friendly key error.
                            if (response.code == 400 || response.code == 403) {
                                return@withContext "Your Gemini API Key is invalid or unauthorized. Please verify the key entered in AI Studio Secrets: code ${response.code}."
                            }
                            
                            // Otherwise, wait 500ms and try again
                            if (attempt == 1) {
                                delay(500)
                            }
                        }
                    }
                } catch (e: IOException) {
                    lastErrorMessage = e.message ?: "Network timeout or connection error"
                    Log.w(TAG, "Network exception for model ${model} (attempt ${attempt})", e)
                    if (attempt == 1) {
                        delay(500)
                    }
                } catch (e: Exception) {
                    lastErrorMessage = e.message ?: "Unknown runtime error"
                    Log.e(TAG, "Non-IO exception during Generative AI execution for model ${model}", e)
                    break // Non-network errors should skip to the next model
                }
            }
        }
        
        // If we reach here, all models and attempts failed
        "Error contacting ERP Assistant (Last error code: ${lastErrorCode}). ${lastErrorMessage}. Please check your network and ensure your Gemini API key from AI Studio is active."
    }
}
