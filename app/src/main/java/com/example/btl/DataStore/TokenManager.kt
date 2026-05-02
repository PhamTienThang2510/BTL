package com.example.btl.DataStore

import android.content.Context
import android.util.Log
import kotlinx.coroutines.runBlocking

class TokenManager(private val context: Context) {
    companion object {
        private const val TAG = "TokenManager"
    }

    /**
     * Save token to DataStore (blocking call for immediate persistence)
     * Used during login to ensure token is persisted before proceeding
     */
    fun saveToken(token: String) {
        Log.d(TAG, "✅ Saving token: ${token.take(20)}...")
        runBlocking {
            DataStoreManager.saveToken(context, token)
            Log.d(TAG, "✅ Token saved successfully")
        }
    }

    /**
     * Get token synchronously (blocking)
     * ⚠️ DEPRECATED: Use getTokenFlow() for reactive updates
     * This blocks the calling thread and may cause ANR on low-end devices
     */
    @Deprecated(
        message = "Use getTokenFlow() for reactive updates",
        replaceWith = ReplaceWith("getTokenFlow()")
    )
    fun getToken(): String? {
        return try {
            val token = runBlocking {
                DataStoreManager.getToken(context)
            }
            if (token != null) {
                Log.d(TAG, "✅ Token retrieved: ${token.take(20)}...")
            } else {
                Log.w(TAG, "⚠️ Token is NULL in getToken()")
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error retrieving token: ${e.message}", e)
            null
        }
    }

    /**
     * Clear token from DataStore (blocking call)
     */
    fun clearToken() {
        Log.d(TAG, "🗑️ Clearing token")
        runBlocking {
            DataStoreManager.clearToken(context)
        }
    }

    /**
     * Get token as Flow for reactive updates
     * ✅ RECOMMENDED: Use this to react to token changes automatically
     * Non-blocking, works perfectly with lifecycleScope.launch
     */
    fun getTokenFlow() = DataStoreManager.getTokenFlow(context)
}

