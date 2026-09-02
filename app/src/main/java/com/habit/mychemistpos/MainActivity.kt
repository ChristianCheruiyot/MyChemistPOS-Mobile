package com.habit.mychemistpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.habit.mychemistpos.data.repository.impl.FirestoreSyncManager
import com.habit.mychemistpos.ui.navigation.AppNavGraph
import com.habit.mychemistpos.ui.theme.MyChemistPOSTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var syncManager: FirestoreSyncManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Must be called BEFORE super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Trigger database sync on startup if already authenticated
        if (FirebaseAuth.getInstance().currentUser != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                syncManager.syncAllFromCloud()
            }
        }

        setContent {
            MyChemistPOSTheme {
                AppNavGraph()
            }
        }
    }
}