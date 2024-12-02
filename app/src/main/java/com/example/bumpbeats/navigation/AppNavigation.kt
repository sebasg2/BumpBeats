package com.example.bumpbeats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bumpbeats.ui.screens.ECGScreen
import com.example.bumpbeats.ui.screens.HeartRateScreen
import com.example.bumpbeats.ui.screens.SignInScreen
import com.example.bumpbeats.ui.screens.SignUpScreen
import com.example.bumpbeats.ui.screens.SuccessScreen
import com.example.bumpbeats.ui.screens.WelcomeScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = "welcome") {
        // Welcome Screen
        composable("welcome") {
            WelcomeScreen(
                onSignInClick = { navController.navigate("signin") },
                onSignUpClick = { navController.navigate("signup") }
            )
        }

        // Sign In Screen
        composable("signin") {
            SignInScreen(
                onSignInSuccess = { navController.navigate("success") },
                onSignInError = { errorMessage ->
                    println("Sign-in error: $errorMessage")
                }
            )
        }

        // Sign Up Screen
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate("success") },
                onSignUpError = { errorMessage ->
                    println("Sign-up error: $errorMessage")
                }
            )
        }

        // Success Screen
        composable("success") {
            SuccessScreen(
                onNavigateToHeartRate = { navController.navigate("heartRate") },
                onNavigateToECG = { navController.navigate("ecg") }
            )
        }

        // Heart Rate Screen
        composable("heartRate") {
            HeartRateScreen()
        }

        // ECG Screen
        composable("ecg") {
            ECGScreen()
        }
    }
}

