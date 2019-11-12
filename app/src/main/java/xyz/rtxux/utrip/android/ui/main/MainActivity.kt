package xyz.rtxux.utrip.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseActivity
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.repository.AuthRepository
import xyz.rtxux.utrip.android.ui.auth.AuthActivity

class MainActivity : BaseActivity() {
    override fun getLayoutResId(): Int = R.layout.activity_main

    private val authRepository by lazy { AuthRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        launch {
            val validationResult = authRepository.validate()
            when (validationResult) {
                is UResult.Success -> return@launch
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

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_explore,
                R.id.navigation_track,
                R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
}
