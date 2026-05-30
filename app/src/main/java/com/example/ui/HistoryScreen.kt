package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookingEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: AmbunalGoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val historyList by viewModel.bookingHistoryList.collectAsState()

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B12)) // Dark slate database ledger background
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 1. TOP BAR with back and Clear All
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
                        .background(Color(0xFF1B2C46))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Riwayat Pesanan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Log Aktivitas Ambulan Kamu",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                if (historyList.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearAllHistory() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF2C1E21))
                            .testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Hapus Semua",
                            tint = Color(0xFFFF5252)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. RETRIEVE RECORD LIST OR EMPTY STATE FOR THE BOOKINGS
            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A2F4C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryToggleOff,
                                contentDescription = "History Empty",
                                tint = Color.LightGray,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Log Masih Kosong",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Silakan pesan Ambulan Jenazah, Ambulan Pasien, SOS Darurat atau Reservasi Event terlebih dahulu. Riwayat Anda akan tercatat di sini secara otomatis.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyList, key = { it.id }) { entity ->
                        HistoryItemRow(
                            booking = entity,
                            dateString = dateFormatter.format(Date(entity.timestamp)),
                            onWhatsAppClick = {
                                // Dynamic trigger dispatching exact raw payload
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=6281225044520&text=${java.net.URLEncoder.encode(entity.whatsAppText, "UTF-8")}")
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(
    booking: BookingEntity,
    dateString: String,
    onWhatsAppClick: () -> Unit,
    viewModel: AmbunalGoViewModel
) {
    val serviceColor = when (booking.serviceType) {
        "JENAZAH" -> Color(0xFFFFCC00)
        "PASIEN" -> Color(0xFF00E676)
        "SOS" -> Color(0xFFFF1744)
        "EVENT" -> Color(0xFF2979FF)
        else -> Color.Gray
    }

    val serviceIcon = when (booking.serviceType) {
        "JENAZAH" -> Icons.Default.AirlineSeatFlat
        "PASIEN" -> Icons.Default.LocalHospital
        "SOS" -> Icons.Default.Campaign
        "EVENT" -> Icons.Default.Festival
        else -> Icons.Default.Place
    }

    val serviceLabel = when (booking.serviceType) {
        "JENAZAH" -> "Ambulan Jenazah"
        "PASIEN" -> "Ambulan Pasien"
        "SOS" -> "DARURAT SOS"
        "EVENT" -> "Ambulan Event"
        else -> "Layanan Medis"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${booking.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Icon + Category + Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(serviceColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = serviceIcon,
                        contentDescription = null,
                        tint = serviceColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = serviceLabel,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = dateString,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                // Delete entity button
                // (Let's make sure they can wipe custom logs)
                IconButton(
                    onClick = {
                        // Triggers silent deletion inside database
                        // To clear specific record
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    // Let's hide direct delete or implement it in entity click if needed
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: Pickup -> Dropoff details
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text("📍", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = booking.pickupName,
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        lineHeight = 14.sp
                    )
                }

                if (booking.serviceType != "SOS" && booking.serviceType != "EVENT") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🏁", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = booking.dropoffName,
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Distance / Pricing + WhatsApp Resend Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Total Fare & Jarak
                Column {
                    if (booking.distance > 0.0) {
                        Text(
                            text = "Jarak: ${booking.distance} km",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (booking.totalFare > 0.0) {
                        Text(
                            text = viewModel.formatIDR(booking.totalFare),
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    } else {
                        // SOS or Event
                        Text(
                            text = if (booking.serviceType == "SOS") "SIAGA TIM DARURAT" else "PROPOSAL DETAILED",
                            color = serviceColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Resend Message Action Button
                Button(
                    onClick = onWhatsAppClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(11.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim Ulang", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White))
                    }
                }
            }
        }
    }
}
