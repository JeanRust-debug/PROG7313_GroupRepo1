package com.clearcash.app.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.clearcash.app.R
import com.clearcash.app.databinding.ActivityMainBinding
import com.clearcash.app.ui.auth.LoginActivity
import com.clearcash.app.ui.budget.BudgetActivity
import com.clearcash.app.utils.SessionManager

// The main screen of the app — hosts all 5 fragments via bottom navigation
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        // Attach the toolbar as the app's action bar
        setSupportActionBar(binding.toolbar)

        // Get the NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // These are the top-level screens — no back arrow will show for them
        val topLevelDestinations = setOf(
            R.id.dashboardFragment,
            R.id.expenseListFragment,
            R.id.categoryFragment,
            R.id.spendingGraphFragment,
            R.id.rewardsFragment
        )

        // Connect the toolbar title to the current navigation destination
        val appBarConfig = AppBarConfiguration(topLevelDestinations)
        setupActionBarWithNavController(navController, appBarConfig)

        // Connect the bottom navigation bar to the NavController
        binding.bottomNav.setupWithNavController(navController)
    }

    // Inflate the options menu (Set Budget + Logout)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handle toolbar menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_budget -> {
                startActivity(Intent(this, BudgetActivity::class.java))
                true
            }
            R.id.action_logout -> {
                confirmLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Show a confirmation dialog before logging the user out
    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                // Clear the saved session and go back to the login screen
                session.clearSession()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    // Clear the back stack so the user cannot navigate back
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Handle the toolbar back arrow for sub-destinations
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}
