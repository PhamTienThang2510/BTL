package com.example.btl.DataStore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

object DataStoreManager {
    private val TOKEN_KEY = stringPreferencesKey("access_token")
    private val SEARCH_HISTORY_KEY = stringPreferencesKey("search_history")
    private const val TAG = "DataStoreManager"

    suspend fun saveToken(context: Context, token: String) {
        try {
            Log.d(TAG, "💾 Saving token to DataStore: ${token.take(20)}...")
            context.dataStore.edit { preferences ->
                preferences[TOKEN_KEY] = token
            }
            Log.d(TAG, "✅ Token saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving token: ${e.message}", e)
        }
    }

    fun getTokenFlow(context: Context): Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    suspend fun getToken(context: Context): String? {
        return try {
            Log.d(TAG, "📖 Reading token from DataStore...")
            val token = context.dataStore.data.first()[TOKEN_KEY]
            if (token != null) {
                Log.d(TAG, "✅ Token retrieved: ${token.take(20)}...")
            } else {
                Log.w(TAG, "⚠️ Token is NULL in DataStore")
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reading token: ${e.message}", e)
            null
        }
    }

    suspend fun clearToken(context: Context) {
        try {
            Log.d(TAG, "🗑️ Clearing token from DataStore")
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
            Log.d(TAG, "✅ Token cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error clearing token: ${e.message}", e)
        }
    }

    suspend fun saveSearchHistory(context: Context, queries: List<String>) {
        try {
            val jsonArray = JSONArray()
            queries.forEach { jsonArray.put(it) }
            context.dataStore.edit { preferences ->
                preferences[SEARCH_HISTORY_KEY] = jsonArray.toString()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving search history: ${e.message}", e)
        }
    }

    suspend fun getSearchHistory(context: Context): List<String> {
        return try {
            val raw = context.dataStore.data.first()[SEARCH_HISTORY_KEY]
            if (raw.isNullOrBlank()) return emptyList()

            val jsonArray = JSONArray(raw)
            val result = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                result.add(jsonArray.optString(i))
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reading search history: ${e.message}", e)
            emptyList()
        }
    }
}
