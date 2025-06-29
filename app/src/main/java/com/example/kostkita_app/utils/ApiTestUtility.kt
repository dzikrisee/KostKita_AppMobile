package com.example.kostkita_app.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiTestUtility {

    companion object {
        private const val TAG = "ApiTestUtility"
        private const val HEALTH_CHECK_URL = "https://kostkitaapi-production.up.railway.app/health"

        suspend fun testApiConnection(): ApiConnectionResult {
            return withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Testing API connection to: $HEALTH_CHECK_URL")

                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val request = Request.Builder()
                        .url(HEALTH_CHECK_URL)
                        .get()
                        .build()

                    val response = client.newCall(request).execute()

                    when {
                        response.isSuccessful -> {
                            val body = response.body?.string()
                            Log.d(TAG, "API connection successful: $body")
                            ApiConnectionResult.Success(body ?: "API is reachable")
                        }
                        response.code == 404 -> {
                            ApiConnectionResult.Error("Health endpoint not found. API may have different structure.")
                        }
                        else -> {
                            ApiConnectionResult.Error("HTTP ${response.code}: ${response.message}")
                        }
                    }

                } catch (e: UnknownHostException) {
                    Log.e(TAG, "DNS resolution failed", e)
                    ApiConnectionResult.Error("Cannot resolve host. Check internet connection.")
                } catch (e: ConnectException) {
                    Log.e(TAG, "Connection failed", e)
                    ApiConnectionResult.Error("Cannot connect to server. Server may be down.")
                } catch (e: SocketTimeoutException) {
                    Log.e(TAG, "Connection timeout", e)
                    ApiConnectionResult.Error("Connection timeout. Server is not responding.")
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error", e)
                    ApiConnectionResult.Error("Unexpected error: ${e.message}")
                }
            }
        }

        suspend fun testAuthEndpoint(): ApiConnectionResult {
            return withContext(Dispatchers.IO) {
                try {
                    val authUrl = "https://kostkitaapi-production.up.railway.app/api/auth/login"
                    Log.d(TAG, "Testing auth endpoint: $authUrl")

                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url(authUrl)
                        .post(okhttp3.RequestBody.create(
                            "application/json".toMediaTypeOrNull(),
                            """{"username":"test","password":"test"}"""
                        ))
                        .build()

                    val response = client.newCall(request).execute()

                    when (response.code) {
                        401 -> ApiConnectionResult.Success("Auth endpoint is working (401 Unauthorized as expected)")
                        400 -> ApiConnectionResult.Success("Auth endpoint is working (400 Bad Request as expected)")
                        200 -> ApiConnectionResult.Success("Auth endpoint is working (200 OK)")
                        else -> ApiConnectionResult.Error("Unexpected response: ${response.code}")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Auth endpoint test failed", e)
                    ApiConnectionResult.Error("Auth test failed: ${e.message}")
                }
            }
        }
    }
}

sealed class ApiConnectionResult {
    data class Success(val message: String) : ApiConnectionResult()
    data class Error(val message: String) : ApiConnectionResult()
}