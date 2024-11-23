package com.example.bumpbeats.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bumpbeats.ui.screens.HeartRateScreen
import com.example.bumpbeats.ui.screens.SignUpScreen
import com.example.bumpbeats.ui.screens.SuccessScreen
import com.example.bumpbeats.ui.screens.WelcomeScreen


@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(
                onSignInClick = { /* Navigate to Sign In screen if implemented */ },
                onSignUpClick = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate("success") },
                onSignUpError = { errorMessage ->
                    println("Sign-up error: $errorMessage")
                }
            )
        }

        composable("success") {
            SuccessScreen(onNavigateToHeartRate = {
                navController.navigate("heartRate")
            })
        }

        composable("heartRate") {
            HeartRateScreen() // Remove the context parameter
        }
    }
}
