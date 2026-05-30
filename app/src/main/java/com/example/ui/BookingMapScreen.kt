package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
fun BookingMapScreen(
    viewModel: AmbunalGoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val selectedService by viewModel.selectedServiceType.collectAsState()
    val pinMode by viewModel.pinSetupMode.collectAsState()
    val pickupName by viewModel.pickupLocationName.collectAsState()
    val dropoffName by viewModel.dropoffLocationName.collectAsState()
    val distance by viewModel.calculatedDistance.collectAsState()
    val fare by viewModel.calculatedFare.collectAsState()
    val notes by viewModel.bookingNotes.collectAsState()

    val pickupLat by viewModel.pickupManualLat.collectAsState()
    val pickupLng by viewModel.pickupManualLng.collectAsState()
    val dropoffLat by viewModel.dropoffManualLat.collectAsState()
    val dropoffLng by viewModel.dropoffManualLng.collectAsState()

    val title = if (selectedService == ServiceType.JENAZAH) "Ambulan Jenazah" else "Ambulan Pasien"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B12)) // Deep dark theme
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
                        .background(Color(0xFF1B2C46))
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
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Konfigurasi GPS & Penghitungan Jarak",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GoogleMapWebView(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("interactive_google_map_webview")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. CONSOLIDATED LOCATION INPUTS (Atur Jemput dan Atur Antar Jadi Satu)
            var pickupSearchText by remember { mutableStateOf("") }
            var dropoffSearchText by remember { mutableStateOf("") }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("unified_location_setup_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF2979FF).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "📍 SET ALAMAT JEMPUT & ANTAR:",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // 3.1 PICKUP INPUT
                    val isPickupActive = pinMode == "PICKUP"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isPickupActive) Color(0xFF1B2C46) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isPickupActive) Color(0xFF00E676) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setPinSetupMode("PICKUP") }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Lokasi Jemput",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Titik Jemput (Sentuh untuk setel pin di Peta):",
                                    color = if (isPickupActive) Color(0xFF00E676) else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = pickupName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = pickupSearchText,
                                onValueChange = { pickupSearchText = it },
                                placeholder = { Text("Ketik nama daerah/jalan jemput...", fontSize = 11.sp, color = Color.Gray) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("pickup_address_search_input"),
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00E676),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                    focusedContainerColor = Color(0xFF09121F),
                                    unfocusedContainerColor = Color(0xFF09121F),
                                    cursorColor = Color(0xFF00E676)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    viewModel.setPinSetupMode("PICKUP")
                                    viewModel.searchAddressText(pickupSearchText, isPickup = true, context = context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                modifier = Modifier.height(44.dp).testTag("pickup_search_go_button"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Text("Cari", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3.2 DROPOFF INPUT
                    val isDropoffActive = pinMode == "DROPOFF"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDropoffActive) Color(0xFF1B2C46) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isDropoffActive) Color(0xFFFF1744) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setPinSetupMode("DROPOFF") }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Tujuan Antar",
                                tint = Color(0xFFFF1744),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tujuan Antar (Sentuh untuk setel pin di Peta):",
                                    color = if (isDropoffActive) Color(0xFFFF1744) else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = dropoffName,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = dropoffSearchText,
                                onValueChange = { dropoffSearchText = it },
                                placeholder = { Text("Ketik nama RS/pemakaman/jalan...", fontSize = 11.sp, color = Color.Gray) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("dropoff_address_search_input"),
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF1744),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                    focusedContainerColor = Color(0xFF09121F),
                                    unfocusedContainerColor = Color(0xFF09121F),
                                    cursorColor = Color(0xFFFF1744)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    viewModel.setPinSetupMode("DROPOFF")
                                    viewModel.searchAddressText(dropoffSearchText, isPickup = false, context = context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744)),
                                modifier = Modifier.height(44.dp).testTag("dropoff_search_go_button"),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Text("Cari", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. LANDMARKS & HOSPITAL QUICK PRESETS ROW (Pilihan Preset Tempat)
            Text(
                text = "🏥 PILIHAN REKOMENDASI TEMPAT CEPAT (Kategori Sesuai Pilihan):",
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("landmark_presets_lazy_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.mapPresetLocations) { preset ->
                    val isApplicableForActive = if (pinMode == "PICKUP") {
                        pickupName == preset.name
                    } else {
                        dropoffName == preset.name
                    }

                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { viewModel.applyPreset(preset) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isApplicableForActive) Color(0xFF1B2C46) else Color(0xFF101B2B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isApplicableForActive) {
                                if (pinMode == "PICKUP") Color(0xFF00E676) else Color(0xFFFF1744)
                            } else {
                                Color.Gray.copy(alpha = 0.2f)
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (preset.iconType) {
                                        "HOSPITAL" -> Icons.Default.LocalHospital
                                        "CEMETERY" -> Icons.Default.Inbox
                                        "HOME" -> Icons.Default.Home
                                        else -> Icons.Default.Place
                                    },
                                    contentDescription = preset.iconType,
                                    tint = if (pinMode == "PICKUP") Color(0xFF00E676) else Color(0xFFFF1744),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset.iconType,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            Text(
                                text = preset.address,
                                color = Color.LightGray,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. LAYANAN TAMBAHAN (Hanya muncul jika memilih Ambulan Jenazah)
            if (selectedService == ServiceType.JENAZAH) {
                val selectedPeti by viewModel.selectedPeti.collectAsState()
                val selectedFormalin by viewModel.selectedFormalin.collectAsState()

                Text(
                    text = "📦 LAYANAN TAMBAHAN JENAZAH:",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("accessories_selection_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFCC00).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.togglePeti() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = "Peti Jenazah",
                                    tint = Color(0xFFFFCC00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Peti Jenazah (Varnish Premium)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Lengkap dengan kain satin hias", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+ ${viewModel.formatIDR(1800000.0)}",
                                    color = Color(0xFFFFCC00),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Checkbox(
                                    checked = selectedPeti,
                                    onCheckedChange = { viewModel.togglePeti() },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFFFCC00),
                                        checkmarkColor = Color.Black
                                    )
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleFormalin() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.Science,
                                    contentDescription = "Formalin & Pengawetan",
                                    tint = Color(0xFFFFCC00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Pengawetan & Formalin Medis", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Oleh tim dokter/medis bersertifikat", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+ ${viewModel.formatIDR(1800000.0)}",
                                    color = Color(0xFFFFCC00),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Checkbox(
                                    checked = selectedFormalin,
                                    onCheckedChange = { viewModel.toggleFormalin() },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFFFCC00),
                                        checkmarkColor = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. DETAILED TRIP INVOICE (Rincian Biaya & Jarak Tempuh)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF122035)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🧾 POSISI & KALKULASI PESANAN (REALTIME):",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Titik Penjemputan GPS:", color = Color.Gray, fontSize = 10.sp)
                            Text(pickupName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFFF1744), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Titik Pengantaran GPS:", color = Color.Gray, fontSize = 10.sp)
                            Text(dropoffName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.2f))

                    // Live breakdown parameters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jarak Tempuh Peta Google:", color = Color.LightGray, fontSize = 12.sp)
                        Text("$distance km", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tarif 5 Km Pertama (Flat):", color = Color.LightGray, fontSize = 12.sp)
                        Text(viewModel.formatIDR(150000.0), color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (distance > 5.0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val extraDistance = distance - 5.0
                            Text("Tambahan Tarif (+${String.format("%.1f", extraDistance)} km x Rp 13.500/km):", color = Color.LightGray, fontSize = 11.sp)
                            Text(viewModel.formatIDR(extraDistance * 13500.0), color = Color(0xFFFF5252), fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (selectedService == ServiceType.JENAZAH) {
                        val selectedPeti by viewModel.selectedPeti.collectAsState()
                        val selectedFormalin by viewModel.selectedFormalin.collectAsState()

                        if (selectedPeti) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Layanan Peti Jenazah:", color = Color.LightGray, fontSize = 11.sp)
                                Text(viewModel.formatIDR(1800000.0), color = Color(0xFFFFCC00), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (selectedFormalin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Layanan Formalin Medis:", color = Color.LightGray, fontSize = 11.sp)
                                Text(viewModel.formatIDR(1800000.0), color = Color(0xFFFFCC00), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL ESTIMASI BIAYA:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = viewModel.formatIDR(fare),
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. NOTES SECTION (Catatan Medis Korban)
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.setBookingNotes(it) },
                label = { Text("Catatan Medis Tambahan (contoh: Butuh Oksigen)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_notes_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray,
                    focusedBorderColor = Color(0xFFFF1744),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                    focusedLabelColor = Color(0xFFFF1744),
                    unfocusedLabelColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 8. FINAL DISPATCH TO WHATSAPP ACTION
            Button(
                onClick = { viewModel.sendAmbulanceBookingToWhatsApp(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_booking_whatsapp_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF25D366) // WhatsApp Official Hex Green
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "KIRIM PESANAN KE WHATSAPP",
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
