package com.example.data

import kotlinx.coroutines.flow.Flow

class BookingRepository(private val bookingDao: BookingDao) {
    val allBookings: Flow<List<BookingEntity>> = bookingDao.getAllBookings()

    suspend fun insert(booking: BookingEntity) {
        bookingDao.insertBooking(booking)
    }

    suspend fun delete(booking: BookingEntity) {
        bookingDao.deleteBooking(booking)
    }

    suspend fun clearAll() {
        bookingDao.clearAllBookings()
    }
}
