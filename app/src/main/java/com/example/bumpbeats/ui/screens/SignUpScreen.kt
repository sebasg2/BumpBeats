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
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onSignUpError: (String) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val age = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Firebase instances
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Password validation states
    val hasMinLength = remember { mutableStateOf(false) }
    val hasUppercase = remember { mutableStateOf(false) }
    val hasNumber = remember { mutableStateOf(false) }
    val hasSymbol = remember { mutableStateOf(false) }

    // Function to validate password and update states
    fun validatePassword(input: String) {
        hasMinLength.value = input.length >= 8
        hasUppercase.value = input.any { it.isUpperCase() }
        hasNumber.value = input.any { it.isDigit() }
        hasSymbol.value = input.any { !it.isLetterOrDigit() }
    }

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

        // Input fields
        OutlinedTextField(
            value = name.value,
            onValueChange = { name.value = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastName.value,
            onValueChange = { lastName.value = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = age.value,
            onValueChange = { age.value = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = {
                password.value = it
                validatePassword(it)
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Text(text = "Password Requirements:", color = Color.Gray)
            Text(
                text = if (hasMinLength.value) "✔ At least 8 characters" else "✘ At least 8 characters",
                color = if (hasMinLength.value) Color.Green else Color.Red
            )
            Text(
                text = if (hasUppercase.value) "✔ At least 1 uppercase letter" else "✘ At least 1 uppercase letter",
                color = if (hasUppercase.value) Color.Green else Color.Red
            )
            Text(
                text = if (hasNumber.value) "✔ At least 1 number" else "✘ At least 1 number",
                color = if (hasNumber.value) Color.Green else Color.Red
            )
            Text(
                text = if (hasSymbol.value) "✔ At least 1 symbol" else "✘ At least 1 symbol",
                color = if (hasSymbol.value) Color.Green else Color.Red
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit button
        Button(
            onClick = {
                val ageInt = age.value.toIntOrNull() ?: 0
                auth.createUserWithEmailAndPassword(email.value, password.value)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Save user data to Firestore
                            val user = mapOf(
                                "name" to name.value,
                                "lastName" to lastName.value,
                                "age" to ageInt,
                                "email" to email.value
                            )
                            db.collection("users").document(auth.currentUser?.uid ?: "")
                                .set(user)
                                .addOnSuccessListener {
                                    println("Sign-up successful! User data saved.")
                                    errorMessage.value = null // Clear error message
                                    onSignUpSuccess()
                                }
                                .addOnFailureListener { e ->
                                    val error = "Failed to save user data: ${e.message}"
                                    println(error)
                                    errorMessage.value = error
                                    onSignUpError(error)
                                }
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthUserCollisionException) {
                                val error = "This email is already registered. Please use a different email."
                                println(error) // Print the error message
                                errorMessage.value = error
                                onSignUpError(error)
                            } else {
                                val error = "Sign-up failed: ${exception?.message}"
                                println(error) // Print the generic error message
                                errorMessage.value = error
                                onSignUpError(error)
                            }
                        }
                    }
            },
            enabled = hasMinLength.value && hasUppercase.value && hasNumber.value && hasSymbol.value,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Submit")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    BumpBeatsTheme {
        SignUpScreen(onSignUpSuccess = {}, onSignUpError = {})
    }
}
