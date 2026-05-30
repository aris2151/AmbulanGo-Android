package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceType: String, // "JENAZAH", "PASIEN", "SOS", "EVENT"
    val pickupName: String,
    val dropoffName: String,
    val distance: Double,
    val totalFare: Double,
    val additionalInfo: String,
    val timestamp: Long = System.currentTimeMillis(),
    val whatsAppText: String
)
