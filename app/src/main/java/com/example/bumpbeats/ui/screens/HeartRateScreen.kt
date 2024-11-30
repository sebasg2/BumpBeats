package com.example.bumpbeats.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket
import java.net.SocketException

@Composable
fun HeartRateScreen() {
    val bpmValues = remember { mutableStateListOf<Int>() }
    val coroutineScope = rememberCoroutineScope()

    // Mutable states for server IP and port
    var serverIp by remember { mutableStateOf("") }
    var serverPort by remember { mutableStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }
    var connectionError by remember { mutableStateOf(false) }
    var finalBpm by remember { mutableStateOf<Int?>(null) }

    // Define the target number of valid BPM values
    val targetDataPoints = 10

    LaunchedEffect(Unit) {
        // Listen for server broadcast
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val udpSocket = DatagramSocket(55555) // Listening on the same port as the Python broadcast
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)

                udpSocket.receive(packet) // Wait for a broadcast message
                val message = String(packet.data, 0, packet.length)
                val parts = message.split(":")
                serverIp = parts[0]
                serverPort = parts[1].toInt()
                isConnected = true
                udpSocket.close()
            } catch (e: Exception) {
                println("Error receiving broadcast: ${e.message}")
            }
        }
    }

    if (!isConnected) {
        // Display a loading message while waiting for the broadcast
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Waiting for server broadcast...")
        }
    } else {
        // Connect to the server and fetch data
        LaunchedEffect(Unit) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val socket = Socket(serverIp, serverPort)
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    while (bpmValues.size < targetDataPoints) { // Collect until enough valid points
                        val line = reader.readLine() ?: break
                        val value = line.toIntOrNull()

                        // Add only acceptable values (40-120) to the list
                        if (value != null && value in 40..120) {
                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                bpmValues.add(value)
                            }
                        }
                    }

                    // Calculate final BPM after collecting enough points
                    if (bpmValues.isNotEmpty()) {
                        finalBpm = bpmValues.average().toInt()
                    }

                    socket.close()
                } catch (e: SocketException) {
                    println("SocketException: ${e.message}")
                    connectionError = true
                } catch (e: Exception) {
                    println("Error: ${e.message}")
                    connectionError = true
                }
            }
        }

        // Display the BPM
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (connectionError) {
                Text(
                    text = "Connection lost. Please check the server.",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Heart Rate Monitor", modifier = Modifier.padding(16.dp))
                    Text(
                        text = finalBpm?.let { "Average BPM: $it" }
                            ?: "Collecting data...",
                        fontSize = 24.sp, // Set font size with sp
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

