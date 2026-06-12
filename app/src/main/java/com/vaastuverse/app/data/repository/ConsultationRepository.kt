package com.vaastuverse.app.data.repository

import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.dto.AvailabilitySlotRequest
import com.vaastuverse.app.data.dto.BookingResponse
import com.vaastuverse.app.data.dto.GurujiAvailabilityResponse
import com.vaastuverse.app.data.dto.TwoWeekAvailabilityRequest
import com.vaastuverse.app.data.network.ApiClient

class ConsultationRepository {
    private val api get() = ApiClient.api

    suspend fun listGurujiBookings(session: StoredSession): List<BookingResponse> {
        return try {
            api.gurujiBookings(ApiClient.bearer(session.accessToken))
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getTwoWeekAvailability(session: StoredSession): List<GurujiAvailabilityResponse> {
        return try {
            api.getGurujiTwoWeekAvailability(ApiClient.bearer(session.accessToken))
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveTwoWeekAvailability(
        session: StoredSession,
        slots: List<AvailabilitySlotRequest>,
    ): List<GurujiAvailabilityResponse> {
        return api.saveGurujiTwoWeekAvailability(
            ApiClient.bearer(session.accessToken),
            TwoWeekAvailabilityRequest(slots = slots),
        )
    }
}
