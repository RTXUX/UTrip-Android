package xyz.rtxux.utrip.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.runBlocking
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseActivity
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.model.repository.AuthRepository
import xyz.rtxux.utrip.android.ui.auth.AuthActivity

class MainActivity : BaseActivity() {
    override fun getLayoutResId(): Int = R.layout.activity_main

    private val authRepository by lazy { AuthRepository() }

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    private val mainFragmentIds = setOf(
        R.id.navigation_explore,
        R.id.navigation_track,
        R.id.navigation_profile
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        runBlocking {
            val validationResult = authRepository.validate()
            when (validationResult) {
                is UResult.Success -> {
                    RetrofitClient.userId = validationResult.data.userId
                    return@runBlocking
                }
                is UResult.Error -> {
                    Toast.makeText(
                        applicationContext,
                        validationResult.exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this@MainActivity, AuthActivity::class.java))
                    this@MainActivity.finish()
                }
            }
        }
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val appBarConfiguration = AppBarConfiguration(mainFragmentIds)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (mainFragmentIds.contains(destination.id)) {
                navView.visibility = View.VISIBLE
            } else {
                navView.visibility = View.GONE
            }
        }
    }

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp() || super.onNavigateUp()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
