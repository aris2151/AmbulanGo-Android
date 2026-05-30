package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookingEntity
import com.example.data.BookingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.location.Geocoder
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.*

enum class AppScreen {
    LOGIN,
    HOME,
    BOOKING,
    SOS,
    EVENT,
    HISTORY
}

enum class ServiceType {
    JENAZAH,
    PASIEN
}

// Preset location model
data class MapPreset(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val iconType: String // "HOSPITAL", "HOME", "CEMETERY", "EVENT", "PUBLIC"
)

class AmbunalGoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookingRepository

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Login Data
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Active Service Selection
    private val _selectedServiceType = MutableStateFlow(ServiceType.PASIEN)
    val selectedServiceType: StateFlow<ServiceType> = _selectedServiceType.asStateFlow()

    // Preset list for custom GPS Map simulator
    val mapPresetLocations = listOf(
        MapPreset("Rumah Sakit Cipto Mangunkusumo (RSCM)", "Jl. Diponegoro No.71, Kenari, Jkt Pusat", -6.1963, 106.8475, "HOSPITAL"),
        MapPreset("RSUP Fatmawati", "Jl. RS. Fatmawati Raya, Cilandak, Jkt Selatan", -6.2952, 106.7958, "HOSPITAL"),
        MapPreset("Rumah Sakit Dharmais", "Jl. Letjen S. Parman No.84, Palmerah, Jkt Barat", -6.1856, 106.7974, "HOSPITAL"),
        MapPreset("TPU Pondok Ranggon (Pemakaman)", "Kec. Cipayung, Jakarta Timur", -6.3688, 106.9171, "CEMETERY"),
        MapPreset("TPU Kalibata (Pemakaman Pahlawan)", "Pancoran, Jakarta Selatan", -6.2573, 106.8528, "CEMETERY"),
        MapPreset("Stadion Utama Gelora Bung Karno (GBK)", "Gelora, Kec. Tanah Abang, Jkt Pusat", -6.2183, 106.8021, "EVENT"),
        MapPreset("JIExpo Kemayoran (Exhibition Center)", "Pademangan, Jakarta Utara", -6.1497, 106.8458, "EVENT"),
        MapPreset("Rumah Saya (Lokasi Terkini)", "Jl. Kebon Sirih No.40, Menteng, Jkt Pusat (GPS)", -6.2088, 106.8456, "HOME"),
        MapPreset("Klinik Pratama Sehat Selamanya", "Jl. Jenderal Sudirman No.12, Jkt Pusat", -6.2297, 106.8166, "HOSPITAL")
    )

    // Map Selections Setup
    private val _pickupPreset = MutableStateFlow<MapPreset>(mapPresetLocations[7]) // Default: Rumah Saya
    val pickupPreset: StateFlow<MapPreset> = _pickupPreset.asStateFlow()

    private val _dropoffPreset = MutableStateFlow<MapPreset>(mapPresetLocations[0]) // Default: RSCM
    val dropoffPreset: StateFlow<MapPreset> = _dropoffPreset.asStateFlow()

    // Custom Map Sliders Coordinate (representing offsets or latitude changes user drags)
    private val _pickupManualLat = MutableStateFlow(-6.2088)
    val pickupManualLat: StateFlow<Double> = _pickupManualLat.asStateFlow()

    private val _pickupManualLng = MutableStateFlow(106.8456)
    val pickupManualLng: StateFlow<Double> = _pickupManualLng.asStateFlow()

    private val _dropoffManualLat = MutableStateFlow(-6.1963)
    val dropoffManualLat: StateFlow<Double> = _dropoffManualLat.asStateFlow()

    private val _dropoffManualLng = MutableStateFlow(106.8475)
    val dropoffManualLng: StateFlow<Double> = _dropoffManualLng.asStateFlow()

    // Flag whether dragging map or preset is active
    private val _isUsingManualPickup = MutableStateFlow(false)
    val isUsingManualPickup: StateFlow<Boolean> = _isUsingManualPickup.asStateFlow()

    private val _isUsingManualDropoff = MutableStateFlow(false)
    val isUsingManualDropoff: StateFlow<Boolean> = _isUsingManualDropoff.asStateFlow()

    // Selected Pin Mode for Interactive Map: "PICKUP" or "DROPOFF"
    private val _pinSetupMode = MutableStateFlow("PICKUP")
    val pinSetupMode: StateFlow<String> = _pinSetupMode.asStateFlow()

    // Map Type Selection: "GOOGLE_MAPS" or "SIMULATION"
    private val _mapSourceType = MutableStateFlow("GOOGLE_MAPS")
    val mapSourceType: StateFlow<String> = _mapSourceType.asStateFlow()

    // Real OSRM calculated road distance in KM
    private val _realRoadDistance = MutableStateFlow<Double?>(null)
    val realRoadDistance: StateFlow<Double?> = _realRoadDistance.asStateFlow()

    // Geocoded actual addresses from Google Maps/Leaflet lookup
    private val _pickupManualAddress = MutableStateFlow("")
    val pickupManualAddress: StateFlow<String> = _pickupManualAddress.asStateFlow()

    private val _dropoffManualAddress = MutableStateFlow("")
    val dropoffManualAddress: StateFlow<String> = _dropoffManualAddress.asStateFlow()

    // Booking notes
    private val _bookingNotes = MutableStateFlow("")
    val bookingNotes: StateFlow<String> = _bookingNotes.asStateFlow()

    // Dead Body extra accessories (Peti & Formalin)
    private val _selectedPeti = MutableStateFlow(false)
    val selectedPeti: StateFlow<Boolean> = _selectedPeti.asStateFlow()

    private val _selectedFormalin = MutableStateFlow(false)
    val selectedFormalin: StateFlow<Boolean> = _selectedFormalin.asStateFlow()

    fun togglePeti() {
        _selectedPeti.value = !_selectedPeti.value
    }

    fun toggleFormalin() {
        _selectedFormalin.value = !_selectedFormalin.value
    }

    // --- SOS Screen State Variables ---
    private val _sosVictimCondition = MutableStateFlow("Kritis / Gawat Darurat")
    val sosVictimCondition: StateFlow<String> = _sosVictimCondition.asStateFlow()

    private val _sosVictimCount = MutableStateFlow(1)
    val sosVictimCount: StateFlow<Int> = _sosVictimCount.asStateFlow()

    private val _sosEmergencyType = MutableStateFlow("Serangan Jantung")
    val sosEmergencyType: StateFlow<String> = _sosEmergencyType.asStateFlow()

    private val _sosManualLocationName = MutableStateFlow("Kecelakaan / Lokasi Terkini Saya")
    val sosManualLocationName: StateFlow<String> = _sosManualLocationName.asStateFlow()

    // --- Event Booking Screen State Variables ---
    private val _eventName = MutableStateFlow("")
    val eventName: StateFlow<String> = _eventName.asStateFlow()

    private val _eventDurationDays = MutableStateFlow(1)
    val eventDurationDays: StateFlow<Int> = _eventDurationDays.asStateFlow()

    private val _eventAudienceCount = MutableStateFlow("100 - 500 Peserta")
    val eventAudienceCount: StateFlow<String> = _eventAudienceCount.asStateFlow()

    private val _eventPackageSelection = MutableStateFlow("Paket Siaga (1 Ambulan + 2 Perawat P3K)")
    val eventPackageSelection: StateFlow<String> = _eventPackageSelection.asStateFlow()

    private val _eventLocationDescription = MutableStateFlow("")
    val eventLocationDescription: StateFlow<String> = _eventLocationDescription.asStateFlow()

    // Repository Active Flows
    val bookingHistoryList: StateFlow<List<BookingEntity>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BookingRepository(database.bookingDao())
        bookingHistoryList = repository.allBookings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Navigation and screen management
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun handleLogin(name: String, email: String, phone: String) {
        _userName.value = name.ifBlank { "Pengguna AmbunalGo" }
        _userEmail.value = email.ifBlank { "pengguna@ambunalgo.com" }
        _userPhone.value = phone.ifBlank { "08123456789" }
        _isLoggedIn.value = true
        _currentScreen.value = AppScreen.HOME
    }

    fun handleLogout() {
        _isLoggedIn.value = false
        _currentScreen.value = AppScreen.LOGIN
    }

    fun selectService(service: ServiceType) {
        _selectedServiceType.value = service
        _bookingNotes.value = ""
        _selectedPeti.value = false
        _selectedFormalin.value = false
        // Reset manual drag offsets initially when service changes to presets
        _isUsingManualPickup.value = false
        _isUsingManualDropoff.value = false
        navigateTo(AppScreen.BOOKING)
    }

    // Set interactive setup mode
    fun setPinSetupMode(mode: String) {
        _pinSetupMode.value = mode
    }

    // Apply location Preset
    fun applyPreset(preset: MapPreset) {
        _realRoadDistance.value = null
        if (_pinSetupMode.value == "PICKUP") {
            _pickupPreset.value = preset
            _pickupManualLat.value = preset.lat
            _pickupManualLng.value = preset.lng
            _isUsingManualPickup.value = false
        } else {
            _dropoffPreset.value = preset
            _dropoffManualLat.value = preset.lat
            _dropoffManualLng.value = preset.lng
            _isUsingManualDropoff.value = false
        }
    }

    // Location Reset to User Current Location
    fun useCurrentLocationForPickup() {
        _realRoadDistance.value = null
        val homePreset = mapPresetLocations[7] // Rumah Saya
        _pickupPreset.value = homePreset
        _pickupManualLat.value = homePreset.lat
        _pickupManualLng.value = homePreset.lng
        _isUsingManualPickup.value = false
        _pinSetupMode.value = "PICKUP"
    }

    // Manual Slider / Drag coordinates manipulation
    fun updatePickupCoordinates(lat: Double, lng: Double) {
        _pickupManualLat.value = lat
        _pickupManualLng.value = lng
        _isUsingManualPickup.value = true
        _pickupManualAddress.value = ""
    }

    fun updateDropoffCoordinates(lat: Double, lng: Double) {
        _dropoffManualLat.value = lat
        _dropoffManualLng.value = lng
        _isUsingManualDropoff.value = true
        _dropoffManualAddress.value = ""
    }

    private fun cleanAddress(fullAddr: String): String {
        val parts = fullAddr.split(",")
        return if (parts.size > 3) {
            parts.take(3).joinToString(", ").trim()
        } else {
            fullAddr
        }
    }

    // Perform robust Nominatim or Native Reverse Geocoding in native Kotlin background
    fun reverseGeocodeLocation(lat: Double, lng: Double, isPickup: Boolean, context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            var resolvedAddress = ""
            // 1. Try Native Geocoder first
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale("id", "ID"))
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val first = addresses[0]
                        resolvedAddress = first.getAddressLine(0) ?: ""
                    }
                }
            } catch (e: Exception) {
                // native failed, fallback to OkHttp Nominatim search below
            }

            // 2. Fallback to Nominatim REST API with clean User-Agent via OkHttp
            if (resolvedAddress.isBlank()) {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lng&zoom=18")
                        .header("User-Agent", "AmbunalGoApp/1.0 (arisgunawan2151@gmail.com)")
                        .header("Referer", "https://www.openstreetmap.org")
                        .build()
                    
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val jsonObj = JSONObject(body)
                            val displayName = jsonObj.optString("display_name", "")
                            if (displayName.isNotBlank()) {
                                resolvedAddress = cleanAddress(displayName)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Final fallback
            if (resolvedAddress.isBlank()) {
                resolvedAddress = "Lokasi GPS (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})"
            }

            // Update on UI thread
            withContext(Dispatchers.Main) {
                if (isPickup) {
                    _pickupManualAddress.value = resolvedAddress
                    _isUsingManualPickup.value = true
                } else {
                    _dropoffManualAddress.value = resolvedAddress
                    _isUsingManualDropoff.value = true
                }
            }
        }
    }

    // Search address in native Kotlin
    fun searchAddressText(query: String, isPickup: Boolean, context: android.content.Context) {
        if (query.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            var lat: Double? = null
            var lng: Double? = null
            var resolvedAddress = ""

            // 1. Try Native Geocoder
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale("id", "ID"))
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(query, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        lat = address.latitude
                        lng = address.longitude
                        resolvedAddress = address.getAddressLine(0) ?: query
                    }
                }
            } catch (e: Exception) {
                // failed, fallback to REST
            }

            // 2. Fallback to Nominatim REST Search API via OkHttp
            if (lat == null || lng == null) {
                try {
                    val client = OkHttpClient()
                    val encodedQuery = URLEncoder.encode(query, "UTF-8")
                    val request = Request.Builder()
                        .url("https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&limit=1")
                        .header("User-Agent", "AmbunalGoApp/1.0 (arisgunawan2151@gmail.com)")
                        .header("Referer", "https://www.openstreetmap.org")
                        .build()
                    
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrBlank()) {
                            val jsonArray = JSONArray(body)
                            if (jsonArray.length() > 0) {
                                val first = jsonArray.getJSONObject(0)
                                lat = first.optDouble("lat")
                                lng = first.optDouble("lon")
                                val displayName = first.optString("display_name", "")
                                resolvedAddress = cleanAddress(displayName)
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Update UI
            if (lat != null && lng != null) {
                withContext(Dispatchers.Main) {
                    if (isPickup) {
                        _pickupManualLat.value = lat
                        _pickupManualLng.value = lng
                        _pickupManualAddress.value = resolvedAddress
                        _isUsingManualPickup.value = true
                    } else {
                        _dropoffManualLat.value = lat
                        _dropoffManualLng.value = lng
                        _dropoffManualAddress.value = resolvedAddress
                        _isUsingManualDropoff.value = true
                    }
                    _realRoadDistance.value = null // reset road distance to update route line
                }
            }
        }
    }

    fun setMapSourceType(type: String) {
        _mapSourceType.value = type
    }

    fun setPickupManualAddress(address: String) {
        _pickupManualAddress.value = address
    }

    fun setDropoffManualAddress(address: String) {
        _dropoffManualAddress.value = address
    }

    fun updateRealRoadDistance(km: Double) {
        _realRoadDistance.value = km
    }

    // Reactive calculations (Distance and total fare)
    val calculatedDistance: StateFlow<Double> = combine(
        pickupManualLat, pickupManualLng, dropoffManualLat, dropoffManualLng, realRoadDistance
    ) { pLat, pLng, dLat, dLng, realDist ->
        realDist ?: calculateHaversineDistance(pLat, pLng, dLat, dLng)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val calculatedFare: StateFlow<Double> = combine(
        calculatedDistance, selectedServiceType, selectedPeti, selectedFormalin
    ) { dist, serviceType, peti, formalin ->
        var fare = calculateFareForDistance(dist)
        if (serviceType == ServiceType.JENAZAH) {
            if (peti) fare += 1800000.0
            if (formalin) fare += 1800000.0
        }
        fare
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 150000.0)

    val pickupLocationName: StateFlow<String> = combine(
        _isUsingManualPickup, _pickupPreset, _pickupManualLat, _pickupManualLng, _pickupManualAddress
    ) { manual, preset, lat, lng, addr ->
        if (manual) {
            addr.ifBlank { "Lokasi GPS (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})" }
        } else preset.name
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Rumah Saya")

    val dropoffLocationName: StateFlow<String> = combine(
        _isUsingManualDropoff, _dropoffPreset, _dropoffManualLat, _dropoffManualLng, _dropoffManualAddress
    ) { manual, preset, lat, lng, addr ->
        if (manual) {
            addr.ifBlank { "Lokasi GPS (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})" }
        } else preset.name
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "RSCM")

    // Core Haversine calculator
    private fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of Earth in KM
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1.0 - a))
        val distance = r * c
        // Return a realistic formatted distance (rounded to 1 decimal place, minimum 0.1)
        return max(0.1, (round(distance * 10) / 10.0))
    }

    // Harga Tetap 150.000 (< 5 km), selanjutnya dikenakan biyaya tambahan 13.500 perkilometer
    private fun calculateFareForDistance(distance: Double): Double {
        val baseFare = 150000.0
        return if (distance <= 5.0) {
            baseFare
        } else {
            val extraKm = distance - 5.0
            baseFare + (extraKm * 13500.0)
        }
    }

    // Set Booking notes
    fun setBookingNotes(note: String) {
        _bookingNotes.value = note
    }

    // Set SOS options
    fun setSosFields(condition: String, count: Int, type: String, location: String) {
        _sosVictimCondition.value = condition
        _sosVictimCount.value = count
        _sosEmergencyType.value = type
        _sosManualLocationName.value = location
    }

    fun incrementSosVictim(amount: Int) {
        _sosVictimCount.value = max(1, _sosVictimCount.value + amount)
    }

    // Set Event parameters
    fun setEventFields(name: String, duration: Int, audience: String, pack: String, location: String) {
        _eventName.value = name
        _eventDurationDays.value = duration
        _eventAudienceCount.value = audience
        _eventPackageSelection.value = pack
        _eventLocationDescription.value = location
    }

    fun incrementEventDuration(amount: Int) {
        _eventDurationDays.value = max(1, _eventDurationDays.value + amount)
    }

    // Clear history logs
    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    // Helper formatter for IDR Currency
    fun formatIDR(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace("Rp", "Rp ").substringBefore(",")
    }

    // Sending Ambulance Bookings to WhatsApp (Target Number: 081225044520)
    fun sendAmbulanceBookingToWhatsApp(context: Context) {
        viewModelScope.launch {
            val distance = calculatedDistance.value
            val fare = calculatedFare.value
            val pickup = pickupLocationName.value
            val dropoff = dropoffLocationName.value
            val isJenazah = selectedServiceType.value == ServiceType.JENAZAH
            val serviceName = if (isJenazah) "Ambulan Jenazah" else "Ambulan Pasien"
            val notes = bookingNotes.value.ifBlank { "Tidak ada catatan khusus" }
            val fareFormatted = formatIDR(fare)

            var extraAddonsText = ""
            var historyAddonsInfo = ""
            if (isJenazah) {
                if (selectedPeti.value) {
                    extraAddonsText += "• Tambahan Peti Jenazah: ${formatIDR(1800000.0)}\n"
                    historyAddonsInfo += " | Tambah Peti"
                }
                if (selectedFormalin.value) {
                    extraAddonsText += "• Tambahan Formalin Medis: ${formatIDR(1800000.0)}\n"
                    historyAddonsInfo += " | Tambah Formalin"
                }
            }

            val bookingMessage = """
🚨 *AMBUNALGO - PEMESANAN AMBULAN* 🚨
-----------------------------------------
👤 *Pemesan:* ${userName.value}
📞 *Kontak:* ${userPhone.value}
📧 *Email:* ${userEmail.value}

Jenis Layanan: *${serviceName}*
-----------------------------------------
📍 *Titik Penjemputan:*
$pickup

🏁 *Titik Pengantaran:*
$dropoff

📦 *Rincian Tarif:*
• Jarak Tempuh: *${distance} km*
• Tarif Dasar (<= 5 km): ${formatIDR(150000.0)}
• Biyaya Tambahan (> 5 km): ${if (distance > 5.0) formatIDR((distance - 5.0) * 13500.0) else "Rp 0"}
${extraAddonsText}💰 *Total Biaya: $fareFormatted*

📝 *Catatan Korban/Pasien:*
$notes

-----------------------------------------
_Dikirim realtime via aplikasi AmbunalGo_
            """.trimIndent()

            // Save to Room DB History
            val entity = BookingEntity(
                serviceType = if (isJenazah) "JENAZAH" else "PASIEN",
                pickupName = pickup,
                dropoffName = dropoff,
                distance = distance,
                totalFare = fare,
                additionalInfo = "Catatan: $notes$historyAddonsInfo",
                whatsAppText = bookingMessage
            )
            repository.insert(entity)

            // Direct to WhatsApp
            sendToWhatsAppChannel(context, bookingMessage)
        }
    }

    // Sending Emergency SOS details to WhatsApp
    fun sendSosToWhatsApp(context: Context) {
        viewModelScope.launch {
            val location = _sosManualLocationName.value.ifBlank { "Lokasi Koordinat GPS Terkini" }
            val condition = _sosVictimCondition.value
            val count = _sosVictimCount.value
            val type = _sosEmergencyType.value

            val sosMessage = """
🚨🧯 *DARURAT SOS - DISPATCH AMBULAN* 🚨🧯
===================================
*MOHON BANTUAN SEGERA!!!*
===================================

👤 *Pelapor:* ${userName.value}
📞 *Kontak:* ${userPhone.value}

📍 *Detail Lokasi Kejadian:*
$location

⚠️ *Kondisi Darurat:*
• Jenis Kejadian: *${type}*
• Kondisi Korban: *${condition}*
• Jumlah Korban: *${count} Orang*

-----------------------------------
Panggilan Darurat ini dikirimkan langsung menggunakan tombol SOS AmbunalGo. Mohon segera hubungi kami kembali!
            """.trimIndent()

            // Save to Room DB History
            val entity = BookingEntity(
                serviceType = "SOS",
                pickupName = location,
                dropoffName = "Pusat Penanganan Darurat Medis (IGD Terdekat)",
                distance = 0.0,
                totalFare = 0.0,
                additionalInfo = "SOS: $type | Korban: $count | Kondisi: $condition",
                whatsAppText = sosMessage
            )
            repository.insert(entity)

            // Open WhatsApp
            sendToWhatsAppChannel(context, sosMessage)
        }
    }

    // Sending Event details to WhatsApp
    fun sendEventBookingToWhatsApp(context: Context) {
        viewModelScope.launch {
            val name = _eventName.value.ifBlank { "Event Umum" }
            val duration = _eventDurationDays.value
            val audience = _eventAudienceCount.value
            val pack = _eventPackageSelection.value
            val location = _eventLocationDescription.value.ifBlank { "Jakarta Area" }

            val eventMessage = """
📆🚑 *AMBUNALGO - PEMESANAN EVENT COOPERATIVE* 📆🚑
-----------------------------------------
👤 *Penanggung Jawab:* ${userName.value}
📞 *Kontak:* ${userPhone.value}

📌 *Detail Kegiatan:*
• Nama Event: *${name}*
• Lokasi Kegiatan: *${location}*
• Durasi Event: *${duration} Hari*
• Estimasi Estimasi Peserta: *${audience}*

📦 *Paket Layanan Medis:*
*${pack}*

-----------------------------------------
_Permintaan ini bersifat penawaran kerjasama medis AmbunalGo Event_
            """.trimIndent()

            // Save to Room DB History
            val entity = BookingEntity(
                serviceType = "EVENT",
                pickupName = location,
                dropoffName = "Lokasi Pengamanan Event Mandiri",
                distance = 0.0,
                totalFare = 0.0,
                additionalInfo = "Event: $name | Durasi: $duration Hari | Paket: $pack",
                whatsAppText = eventMessage
            )
            repository.insert(entity)

            // Open WhatsApp
            sendToWhatsAppChannel(context, eventMessage)
        }
    }

    // WhatsApp Dispatch Router
    private fun sendToWhatsAppChannel(context: Context, text: String) {
        val phoneNumber = "081225044520"
        val formattedNumber = if (phoneNumber.startsWith("0")) {
            "62" + phoneNumber.substring(1)
        } else {
            phoneNumber
        }

        try {
            val encodedMsg = URLEncoder.encode(text, "UTF-8")
            val url = "https://api.whatsapp.com/send?phone=$formattedNumber&text=$encodedMsg"
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka WhatsApp: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
