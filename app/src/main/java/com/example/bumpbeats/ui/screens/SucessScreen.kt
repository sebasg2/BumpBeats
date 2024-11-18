package com.example.bumpbeats.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.bumpbeats.ui.theme.BumpBeatsTheme

@Composable
fun SuccessScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Sign-up Successful! Welcome to BumpBeats!")
    }
}

@Preview(showBackground = true)
@Composable
fun SuccessScreenPreview() {
    BumpBeatsTheme {
        SuccessScreen()
    }
}


