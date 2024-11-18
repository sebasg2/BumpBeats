package com.example.bumpbeats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.bumpbeats.navigation.AppNavigation
import com.example.bumpbeats.ui.theme.BumpBeatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BumpBeatsTheme {
                val navController = rememberNavController()
                // Only pass navController here
                AppNavigation(navController = navController)
            }
        }
    }
}
