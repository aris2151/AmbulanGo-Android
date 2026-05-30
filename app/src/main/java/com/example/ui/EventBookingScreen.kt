package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EventBookingScreen(
    viewModel: AmbunalGoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val eventName by viewModel.eventName.collectAsState()
    val eventDuration by viewModel.eventDurationDays.collectAsState()
    val eventAudience by viewModel.eventAudienceCount.collectAsState()
    val eventPackage by viewModel.eventPackageSelection.collectAsState()
    val eventLocation by viewModel.eventLocationDescription.collectAsState()

    val audienceOptions = listOf(
        "10 - 100 Orang",
        "100 - 500 Orang",
        "500 - 1500 Orang",
        "> 1500 Orang (Konser/Massal)"
    )

    val packageOptions = listOf(
        "Paket Siaga (1 Ambulan + 2 Perawat P3K)",
        "Paket Dokter (1 Ambulan + 1 Dokter + 1 Perawat)",
        "Paket VIP (2 Ambulan + ICU Portable + Tim Dokter)"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07111E)) // Royal Navy Dark Theme
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
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
                        .background(Color(0xFF14243C))
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
                        text = "Layanan Ambulan Event",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Penyediaan Unit Medis & P3K untuk Kegiatan",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF82B1FF))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. FORM BODY
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF2979FF).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 DATA KEGIATAN & OPERASIONAL:",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Event Name
                    OutlinedTextField(
                        value = eventName,
                        onValueChange = { viewModel.setEventFields(it, eventDuration, eventAudience, eventPackage, eventLocation) },
                        label = { Text("Nama Kegiatan / Event") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = Color(0xFF2979FF),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                            focusedLabelColor = Color(0xFF2979FF),
                            unfocusedLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Event Location
                    OutlinedTextField(
                        value = eventLocation,
                        onValueChange = { viewModel.setEventFields(eventName, eventDuration, eventAudience, eventPackage, it) },
                        label = { Text("Lokasi Tempat Kegiatan (Venue)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_location_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = Color(0xFF2979FF),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                            focusedLabelColor = Color(0xFF2979FF),
                            unfocusedLabelColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Estimasi Jumlah Peserta (LazyRow Filter chips)
                    Text("Estimasi Jumlah Peserta/Pengunjung:", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(audienceOptions) { option ->
                            FilterChip(
                                selected = (eventAudience == option),
                                onClick = { viewModel.setEventFields(eventName, eventDuration, option, eventPackage, eventLocation) },
                                label = { Text(option, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2979FF),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF14243C),
                                    labelColor = Color.LightGray
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Durasi Hari (Interactive Counter)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF14243C))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Durasi Kegiatan:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Unit standby penuh selama kegiatan", color = Color.LightGray.copy(alpha = 0.6f), fontSize = 10.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.incrementEventDuration(-1) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1C2D47))
                            ) {
                                Text("-", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }

                            Text(
                                text = "$eventDuration Hari",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 14.dp),
                                fontSize = 15.sp
                            )

                            IconButton(
                                onClick = { viewModel.incrementEventDuration(1) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2979FF))
                            ) {
                                Text("+", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Paket Layanan Medis
                    Text("Paket Standby Medis Yang Dibutuhkan:", color = Color.LightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    packageOptions.forEach { option ->
                        val isSelected = (eventPackage == option)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.setEventFields(eventName, eventDuration, eventAudience, option, eventLocation) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1A2F4C) else Color(0xFF101B2B)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF2979FF) else Color.Gray.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setEventFields(eventName, eventDuration, eventAudience, option, eventLocation) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2979FF))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = option,
                                    color = if (isSelected) Color.White else Color.LightGray,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Info banner about Event support
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2979FF), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Harga resmi siaga event mengikuti durasi dan ketersediaan peralatan khusus medis yang diperlukan.",
                        color = Color.LightGray,
                        fontSize = 9.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. SUBMIT TO WHATSAPP Button
            Button(
                onClick = { viewModel.sendEventBookingToWhatsApp(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_event_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2979FF)
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "KIRIM BOOKING EVENT KE WA",
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
