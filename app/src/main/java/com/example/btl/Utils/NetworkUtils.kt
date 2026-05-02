package com.example.btl.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission

/**
 * Network Connectivity Utility
 * Provides helper functions to check network status
 */
@Suppress("DEPRECATION")
object NetworkUtils {
    private const val TAG = "NetworkUtils"

    /**
     * Check if device has active internet connection
     *
     * @param context Android context
     * @return true if device is connected to internet, false otherwise
     *
     * Requirements:
     * - Requires android.permission.ACCESS_NETWORK_STATE in AndroidManifest.xml
     * - Requires android.permission.INTERNET in AndroidManifest.xml
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

            val isAvailable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0 (API 23) and above
                val network = connectivityManager.activeNetwork
                if (network == null) {
                    Log.w(TAG, "No active network found")
                    false
                } else {
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    if (capabilities == null) {
                        Log.w(TAG, "Network capabilities not available")
                        false
                    } else {
                        val hasInternet = capabilities.hasCapability(
                            NetworkCapabilities.NET_CAPABILITY_INTERNET
                        )
                        Log.d(TAG, "Network available (Android 6.0+): $hasInternet")
                        hasInternet
                    }
                }
            } else {
                // Android 5.1 (API 22) and below
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                val connected = activeNetworkInfo != null && activeNetworkInfo.isConnected
                Log.d(TAG, "Network available (Android 5.1-): $connected")
                connected
            }
            isAvailable
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability: ${e.message}", e)
            false
        }
    }

    /**
     * Get human-readable network type
     *
     * @param context Android context
     * @return Network type name (WiFi, Mobile, None, etc.)
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun getNetworkTypeName(context: Context): String {
        return try {
            val connectivityManager = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork ?: return "None"
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                    ?: return "Unknown"

                return when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
                    else -> "Other"
                }
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                return when (networkInfo?.type) {
                    ConnectivityManager.TYPE_WIFI -> "WiFi"
                    ConnectivityManager.TYPE_MOBILE -> "Mobile"
                    ConnectivityManager.TYPE_ETHERNET -> "Ethernet"
                    else -> "None"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network type: ${e.message}", e)
            "Unknown"
        }
    }
}

