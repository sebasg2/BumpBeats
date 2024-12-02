package com.example.bumpbeats.ui.screens

import androidx.compose.foundation.layout.* // Import for layouts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.joda.time.LocalDate
import org.joda.time.Weeks

@Composable
fun SuccessScreen(
    onNavigateToHeartRate: () -> Unit,
    onNavigateToECG: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // States for user data
    val name = remember { mutableStateOf("") }
    val weeksPassed = remember { mutableStateOf(0) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // Fetch user data from Firebase
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Fetch user's name and weeksPassed
                        name.value = document.getString("name") ?: "User"
                        val initialWeeks = document.getString("weeksPassed")?.toIntOrNull() ?: 0
                        val signUpDate = document.getString("signUpDate")?.let { LocalDate.parse(it) } ?: LocalDate.now()

                        // Calculate the weeks passed since sign-up
                        val weeksSinceSignUp = Weeks.weeksBetween(signUpDate, LocalDate.now()).weeks
                        weeksPassed.value = initialWeeks + weeksSinceSignUp
                    }
                }
                .addOnFailureListener { e ->
                    errorMessage.value = "Failed to fetch data: ${e.message}"
                }
        } else {
            errorMessage.value = "User not signed in."
        }
    }

    // Screen UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Section: Welcome message and weeks passed
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display error message if any
            errorMessage.value?.let {
                Text(text = it, color = Color.Red, modifier = Modifier.padding(8.dp))
            }

            // Welcome message
            Text(
                text = "Welcome, ${name.value}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(8.dp)
            )

            // Weeks passed as a subtitle
            Text(
                text = "Weeks of pregnancy: ${weeksPassed.value}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(4.dp)
            )
        }

        // Spacer to move buttons to the center
        Spacer(modifier = Modifier.weight(1f))

        // Center Section: BPM and ECG buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNavigateToHeartRate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "View BPM Monitor")
            }

            Button(
                onClick = onNavigateToECG,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "View ECG Monitor")
            }
        }

        // Spacer to balance layout
        Spacer(modifier = Modifier.weight(1f))
    }
}
