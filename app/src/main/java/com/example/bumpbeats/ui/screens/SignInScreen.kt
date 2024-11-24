package com.example.bumpbeats.ui.screens

import androidx.compose.foundation.layout.* // Import for layouts
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.bumpbeats.ui.theme.BumpBeatsTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit,
    onSignInError: (String) -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Firebase Auth instance
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the error message if present
        errorMessage.value?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Input fields for email and password
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Sign In Button
        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email.value, password.value)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            println("Sign-in successful!")
                            errorMessage.value = null // Clear error message
                            onSignInSuccess() // Navigate to SuccessScreen
                        } else {
                            val error = task.exception?.message ?: "Invalid email or password"
                            println(error)
                            errorMessage.value = error
                            onSignInError(error)
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Sign In")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    BumpBeatsTheme {
        SignInScreen(onSignInSuccess = {}, onSignInError = {})
    }
}
