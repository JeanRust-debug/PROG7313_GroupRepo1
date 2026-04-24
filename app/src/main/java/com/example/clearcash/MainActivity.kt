package com.example.clearcash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.clearcash.databinding.ActivityMainBinding

/**
 * Main entry point of ClearCash app.
 * Hosts the NavHostFragment and connects bottom navigation.
 * Author: Muaaz Abdool Gaffoor (ST10443760)
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Top level destinations — no back button on these
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.categoryFragment,
                R.id.expenseFragment,
                R.id.budgetGoalFragment
            )
        )

        // Connects toolbar to nav so back button appears automatically
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)
    }

    // Handles the back button press in the toolbar
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}