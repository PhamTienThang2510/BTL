package com.example.btl.Activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.btl.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log

class ShoppingActivity : AppCompatActivity() {
    private var navController: NavController? = null
    private lateinit var bottomNav: BottomNavigationView

    companion object {
        private const val TAG = "ShoppingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            setContentView(R.layout.activity_shopping)
            Log.d(TAG, "Layout set successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting content view: ${e.message}", e)
            e.printStackTrace()
            return
        }

        try {
            Log.d(TAG, "Setting up navigation...")

            // Setup navigation
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

            if (navHostFragment == null) {
                Log.e(TAG, "NavHostFragment not found!")
                return
            }

            navController = navHostFragment.navController
            Log.d(TAG, "NavController initialized successfully")

            // Setup bottom navigation
            bottomNav = findViewById(R.id.bottom_navigation)

            if (navController != null) {
                bottomNav.setupWithNavController(navController!!)
                Log.d(TAG, "Bottom navigation setup successfully")
            } else {
                Log.e(TAG, "NavController is null after initialization")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during onCreate: ${e.message}", e)
            e.printStackTrace()
        }
    }
}