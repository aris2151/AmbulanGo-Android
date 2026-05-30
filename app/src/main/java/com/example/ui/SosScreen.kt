package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SosScreen(
    viewModel: AmbunalGoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val pPhone by viewModel.userPhone.collectAsState()

    val condition by viewModel.sosVictimCondition.collectAsState()
    val victimCount by viewModel.sosVictimCount.collectAsState()
    val emergencyType by viewModel.sosEmergencyType.collectAsState()
    val locationName by viewModel.sosManualLocationName.collectAsState()

    // Breathing pulse effect for SOS circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_sos")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ), label = "visual_pulse"
    )

    val emergencyOptions = listOf(
        "Kecelakaan Lalu Lintas",
        "Serangan Jantung",
        "Sesak Nafas Berat",
        "Bencana Alam",
        "Melahirkan Darurat",
        "Meninggal Mendadak"
    )

    val conditionOptions = listOf(
        "Kritis / Gawat Darurat",
        "Luka Parah / Berdarah",
        "Kehilangan Kesadaran",
        "Meninggal Dunia",
        "Luka Ringan"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0507)) // Crimson tinted dark theme
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP HEADER with Back Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF351216))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Layanan Darurat SOS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Penerbitan Ambulan Prioritas Utama",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFF8A80))
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. CONCENTRIC PULSING SOS RADAR BUTTON
            Box(
                modifier = Modifier
                    .size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outermost pulse concentric ring
                Box(
                    modifier = Modifier
                        .size(175.dp * pulseScale)
                        .clip(CircleShape)
                        .background(Color(0xFFFF1744).copy(alpha = 0.15f))
                )
                // Middle pulse concentric ring
                Box(
                    modifier = Modifier
                        .size(145.dp * pulseScale)
                        .clip(CircleShape)
                        .background(Color(0xFFFF1744).copy(alpha = 0.25f))
                )
                // Actual interactive SOS button
                Box(
                    modifier = Modifier
                        .size(115.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFFF1744), Color(0xFFD50000))
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        .testTag("pulse_sos_trigger_button")
                        .clickable { viewModel.sendSosToWhatsApp(context) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SOS",
                            style = TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                letterSpacing = 2.sp
                            )
                        )
                        Text(
                            text = "TAP DISPATCH",
                            style = TextStyle(
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }
            }

            Text(
                text = "Pencet tombol merah untuk mengirimkan sinyal SOS cepat koordinat saat ini ke Hotline Call Center",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                color = Color.LightGray.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. FORM FIELDS CONTAINER
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D0F11)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📝 ISI VARIABEL KONDISI DARURAT:",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detail Lokasi Kejadian (Kecelakaan)
                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { viewModel.setSosFields(condition, victimCount, emergencyType, it) },
                        label = { Text("Detail Deskripsi Lokasi Kejadian") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sos_location_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = Color(0xFFFF1744),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                            focusedLabelColor = Color(0xFFFF1744),
                            unfocusedLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Jenis Kejadian (Chips row selection)
                    Text("Jenis Kejadian / Emergency:", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(emergencyOptions) { option ->
                            FilterChip(
                                selected = (emergencyType == option),
                                onClick = { viewModel.setSosFields(condition, victimCount, option, locationName) },
                                label = { Text(option, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF1744),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2C1518),
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Kondisi Korban (Chips row selection)
                    Text("Kondisi Korban Saat Ini:", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(conditionOptions) { option ->
                            FilterChip(
                                selected = (condition == option),
                                onClick = { viewModel.setSosFields(option, victimCount, emergencyType, locationName) },
                                label = { Text(option, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF1744),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF2C1518),
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Jumlah Korban (Interactive Counter)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2D1115))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Jumlah Korban Darurat:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Tetapkan jumlah korban medis", color = Color.LightGray.copy(alpha = 0.6f), fontSize = 10.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.incrementSosVictim(-1) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3D1B1E))
                            ) {
                                Text("-", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }

                            Text(
                                text = "$victimCount Orang",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 14.dp),
                                fontSize = 15.sp
                            )

                            IconButton(
                                onClick = { viewModel.incrementSosVictim(1) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF1744))
                            ) {
                                Text("+", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. SUBMIT TO WHATSAPP Dispatch Call Button
            Button(
                onClick = { viewModel.sendSosToWhatsApp(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_sos_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF1744)
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "KIRIM EMERGENCY SOS SEKARANG",
                        style = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
