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
import com.google.firebase.firestore.FirebaseFirestore
import org.joda.time.LocalDate
import org.joda.time.Weeks

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onSignUpError: (String) -> Unit
) {
    val name = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val birthday = remember { mutableStateOf("") }
    val weeksPassed = remember { mutableStateOf("") }
    val isValidWeeks = remember { mutableStateOf(true) }
    val calculatedDueDate = remember(weeksPassed.value) {
        weeksPassed.value.toIntOrNull()?.let { weeks ->
            if (weeks in 0..40) { // Valid range for weeks of pregnancy
                isValidWeeks.value = true
                LocalDate.now().plusWeeks(40 - weeks).toString() // Calculate due date
            } else {
                isValidWeeks.value = false
                null
            }
        } ?: run {
            isValidWeeks.value = false
            null
        }
    }
    val trimester = remember(weeksPassed.value) {
        weeksPassed.value.toIntOrNull()?.let {
            when {
                it in 0..13 -> "First"
                it in 14..27 -> "Second"
                it >= 28 -> "Third"
                else -> ""
            }
        } ?: ""
    }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val weight = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Password validation states
    val hasMinLength = remember { mutableStateOf(false) }
    val hasUppercase = remember { mutableStateOf(false) }
    val hasNumber = remember { mutableStateOf(false) }
    val hasSymbol = remember { mutableStateOf(false) }

    // Firebase instances
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

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
            label = { Text("First Name") },
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
            value = birthday.value,
            onValueChange = { birthday.value = it },
            label = { Text("Birthday (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = weeksPassed.value,
            onValueChange = { weeksPassed.value = it },
            label = { Text("Number of weeks of pregnancy") },
            modifier = Modifier.fillMaxWidth()
        )
        if (!isValidWeeks.value) {
            Text(
                text = "Please enter a valid number of weeks (0-40).",
                color = Color.Red,
                modifier = Modifier.padding(4.dp)
            )
        } else if (calculatedDueDate != null) {
            Text("Trimester: $trimester", color = Color.Gray)
            Text("Expected Due Date: $calculatedDueDate", color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = weight.value,
            onValueChange = { weight.value = it },
            label = { Text("Weight (kg, optional)") },
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
                auth.createUserWithEmailAndPassword(email.value, password.value)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Save user data to Firestore
                            val user = mapOf(
                                "name" to name.value,
                                "lastName" to lastName.value,
                                "birthday" to birthday.value,
                                "weeksPassed" to weeksPassed.value,
                                "trimester" to trimester,
                                "expectedDueDate" to calculatedDueDate,
                                "weight" to weight.value,
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
                            val error = task.exception?.message ?: "Sign-up failed"
                            println(error)
                            errorMessage.value = error
                            onSignUpError(error)
                        }
                    }
            },
            enabled = isValidWeeks.value && hasMinLength.value && hasUppercase.value && hasNumber.value && hasSymbol.value,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Sign Up")
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

