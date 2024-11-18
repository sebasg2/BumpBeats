package com.example.bumpbeats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bumpbeats.ui.screens.SignUpScreen
import com.example.bumpbeats.ui.screens.WelcomeScreen
import com.example.bumpbeats.ui.screens.SuccessScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = "welcome") {

        // Welcome screen route
        composable("welcome") {
            WelcomeScreen(
                onSignInClick = { /* Navigate to Sign In screen */ },
                onSignUpClick = { navController.navigate("signup") }
            )
        }

        // Sign-up screen route
        composable("signup") {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate("success") },
                onSignUpError = { errorMessage ->
                    // Log the error or display a message (e.g., Toast or Snackbar)
                    println("Sign-up error: $errorMessage")
                }
            )
        }

        // Success screen route
        composable("success") {
            SuccessScreen()
        }
    }
}

