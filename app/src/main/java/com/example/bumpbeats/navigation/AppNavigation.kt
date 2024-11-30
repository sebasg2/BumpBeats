package com.example.bumpbeats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
                onSignInClick = { navController.navigate("signin") }, // Navigate to SignInScreen
                onSignUpClick = { navController.navigate("signup") }  // Navigate to SignUpScreen
            )
        }

        // Sign In Screen
        composable("signin") {
            SignInScreen(
                onSignInSuccess = { navController.navigate("success") }, // Navigate to SuccessScreen
                onSignInError = { errorMessage ->
                    println("Sign-in error: $errorMessage") // Handle error if sign-in fails
                }
            )
        }

        // Sign Up Screen
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate("success") }, // Navigate to SuccessScreen
                onSignUpError = { errorMessage ->
                    println("Sign-up error: $errorMessage") // Handle error if sign-up fails
                }
            )
        }

        // Success Screen
        composable("success") {
            SuccessScreen(onNavigateToHeartRate = {
                navController.navigate("heartRate") // Navigate to HeartRateScreen
            })
        }

        // Heart Rate Monitor Screen
        composable("heartRate") {
            HeartRateScreen() // Load Heart Rate Screen
        }
    }
}
