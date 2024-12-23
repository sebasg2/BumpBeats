package com.example.bumpbeats.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.* // Import for layouts
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket
import java.net.SocketException

@Composable
fun ECGScreen() {
    val ecgData = remember { mutableStateListOf<Float>() }
    val coroutineScope = rememberCoroutineScope()

    // Mutable states for server IP and port
    var serverIp by remember { mutableStateOf("") }
    var serverPort by remember { mutableStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }
    var connectionError by remember { mutableStateOf(false) }
    val bpm = remember { mutableStateOf(0) }
    var showBpm by remember { mutableStateOf(false) } // State to control BPM display

    LaunchedEffect(Unit) {
        // Timer to show BPM after 15 seconds
        coroutineScope.launch(Dispatchers.Main) {
            kotlinx.coroutines.delay(25000) // Wait for 15 seconds
            showBpm = true
        }

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

                    while (isActive) {
                        val line = reader.readLine() ?: break
                        val value = line.toFloatOrNull()
                        if (value != null) {
                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                ecgData.add(value)
                                if (ecgData.size > 100) {
                                    ecgData.removeAt(0) // Keep the list manageable
                                }

                                // Detect peaks and calculate BPM
                                calculateBPM(ecgData, 100, bpm)
                            }
                        }
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

        // Display the ECG data as a graph and final BPM
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (connectionError) {
                Text(
                    text = "Connection lost. Please check the server.",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // ECG Graph using Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp)
                ) {
                    if (ecgData.isNotEmpty()) {
                        // Calculate the step size for X-axis based on available width
                        val stepX = size.width / (ecgData.size - 1).coerceAtLeast(1)

                        // Normalize Y values to fit within the canvas height
                        val maxY = ecgData.maxOrNull() ?: 1f
                        val minY = ecgData.minOrNull() ?: 0f
                        val rangeY = (maxY - minY).coerceAtLeast(1f)

                        // Map data points to canvas coordinates
                        val path = Path().apply {
                            ecgData.forEachIndexed { index, value ->
                                val x = index * stepX
                                val y = size.height - ((value - minY) / rangeY * size.height)
                                if (index == 0) moveTo(x, y) else lineTo(x, y)
                            }
                        }

                        // Draw the path
                        drawPath(
                            path = path,
                            color = Color.Red,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display BPM only if 15 seconds have passed
                if (showBpm) {
                    Text(
                        text = "Final BPM: ${bpm.value}",
                        fontSize = 24.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

fun calculateBPM(
    ecgData: List<Float>,
    samplingRate: Int,
    bpm: MutableState<Int>
) {
    if (ecgData.isEmpty()) return

    // Step 1: Signal Smoothing
    val smoothedData = ecgData.mapIndexed { i, value ->
        when {
            i == 0 -> value // First value (no previous data for smoothing)
            i == ecgData.size - 1 -> value // Last value
            else -> (ecgData[i - 1] + value + ecgData[i + 1]) / 3 // Moving average
        }
    }

    // Step 2: Peak Detection (R-wave detection)
    val threshold = smoothedData.maxOrNull()?.times(0.8f) ?: return
    val rWaveIndices = mutableListOf<Int>()

    // Detect R-wave peaks
    for (i in 1 until smoothedData.size - 1) {
        if (smoothedData[i] > threshold && smoothedData[i] > smoothedData[i - 1] && smoothedData[i] > smoothedData[i + 1]) {
            rWaveIndices.add(i)
        }
    }

    // Step 3: Calculate BPM
    if (rWaveIndices.size > 1) {
        val intervalsInSamples = rWaveIndices.zipWithNext { a, b -> b - a } // Intervals in samples
        val avgIntervalInSeconds = intervalsInSamples.map { it.toFloat() / samplingRate }.average() // Average interval in seconds
        bpm.value = (60 / avgIntervalInSeconds).toInt() // Calculate BPM
    }
}
