package xyz.rtxux.utrip.android.ui.auth

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseActivity

class AuthActivity : BaseActivity() {
    override fun getLayoutResId(): Int = R.layout.activity_auth

    val navController by lazy { findNavController(R.id.auth_nav_host_fragment) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appBarConfiguration = AppBarConfiguration(
            navController.graph
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}
