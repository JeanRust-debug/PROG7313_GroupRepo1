package com.example.clearcash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.clearcash.databinding.ActivityMainBinding

/**
 * Main entry point of ClearCash app.
 * Hosts the NavHostFragment and connects bottom navigation.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding for activity_main layout
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout using ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)

        // Get the NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Connect bottom navigation to NavController
        binding.bottomNav.setupWithNavController(navController)
    }
}