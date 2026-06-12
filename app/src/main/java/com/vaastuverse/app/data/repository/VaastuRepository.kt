package com.vaastuverse.app.data.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.vaastuverse.app.data.PartnerDashboardStats
import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.TokenStore
import com.vaastuverse.app.data.ConsultationOffer
import com.vaastuverse.app.data.dto.BookConsultationRequest
import com.vaastuverse.app.data.dto.BookingResponse
import com.vaastuverse.app.data.dto.DiscoverablePartnerResponse
import com.vaastuverse.app.data.dto.AccountMeResponse
import com.vaastuverse.app.data.dto.ApplicationResponse
import com.vaastuverse.app.data.dto.ApplyRequest
import com.vaastuverse.app.data.dto.AuthResponse
import com.vaastuverse.app.data.dto.CustomerProfileRequest
import com.vaastuverse.app.data.dto.CustomerProfileResponse
import com.vaastuverse.app.data.dto.LoginRequest
import com.vaastuverse.app.data.dto.OtpSendRequest
import com.vaastuverse.app.data.dto.OtpVerifyRequest
import com.vaastuverse.app.data.dto.PartnerProfileResponse
import com.vaastuverse.app.data.dto.RegisterRequest
import com.vaastuverse.app.data.dto.SetPersonaRequest
import com.vaastuverse.app.data.network.ActiveRoleHolder
import com.vaastuverse.app.data.network.ApiClient
import retrofit2.HttpException

class VaastuRepository(private val tokenStore: TokenStore) {

  private val api get() = ApiClient.api

  suspend fun register(phone: String, email: String?, password: String?, displayName: String?): StoredSession =
    persist(api.register(RegisterRequest(phone, email, password, displayName)))

  suspend fun sendOtp(phone: String) {
    try {
      api.sendOtp(OtpSendRequest(phone))
    } catch (e: HttpException) {
      throw mapError(e)
    }
  }

  suspend fun verifyOtp(phone: String, code: String): StoredSession =
    try {
      persist(api.verifyOtp(OtpVerifyRequest(phone, code)))
    } catch (e: HttpException) {
      throw mapError(e)
    }

  suspend fun login(email: String, password: String): StoredSession =
    persist(api.login(LoginRequest(email, password)))

  suspend fun refreshSession(): StoredSession {
    val current = tokenStore.currentSession()
      ?: throw IllegalStateException("Not logged in")
    return persist(api.refresh(current.refreshToken))
  }

  suspend fun getAccountMe(session: StoredSession): AccountMeResponse =
    api.getAccountMe(ApiClient.bearer(session.accessToken))

  suspend fun setPersona(session: StoredSession, persona: String): AccountMeResponse =
    api.setPersona(ApiClient.bearer(session.accessToken), SetPersonaRequest(persona))

  suspend fun deleteAccount(session: StoredSession) {
    api.deleteAccount(ApiClient.bearer(session.accessToken))
    tokenStore.clear()
  }

  suspend fun getCustomerProfile(session: StoredSession): CustomerProfileResponse? {
    return try {
      api.getCustomerProfile(session.userId, ApiClient.bearer(session.accessToken))
    } catch (e: HttpException) {
      val body = e.response()?.errorBody()?.string().orEmpty()
      if (e.code() == 404 || (e.code() == 400 && body.contains("not found", ignoreCase = true))) {
        null
      } else {
        throw mapError(e)
      }
    }
  }

  suspend fun saveCustomerProfile(
    session: StoredSession,
    displayName: String,
    city: String?,
    exists: Boolean,
    dateOfBirth: String? = null,
  ): CustomerProfileResponse {
    val body = CustomerProfileRequest(displayName, city, dateOfBirth = dateOfBirth)
    val auth = ApiClient.bearer(session.accessToken)
    return if (exists) {
      api.updateCustomerProfile(session.userId, auth, body)
    } else {
      api.createCustomerProfile(session.userId, auth, body)
    }
  }

  suspend fun listPartnerProfiles(session: StoredSession): List<PartnerProfileResponse> =
    api.listPartnerProfiles(session.userId, ApiClient.bearer(session.accessToken))

  suspend fun applyPartner(session: StoredSession, requestedRole: String): ApplicationResponse =
    api.applyOnboarding(
      ApiClient.bearer(session.accessToken),
      ApplyRequest(requestedRole),
    )

  suspend fun myApplications(session: StoredSession): List<ApplicationResponse> =
    api.myOnboardingApplications(ApiClient.bearer(session.accessToken))

  suspend fun discoverPartners(session: StoredSession, type: String = "ALL"): List<DiscoverablePartnerResponse> =
    api.discoverPartners(ApiClient.bearer(session.accessToken), type)

  suspend fun loadPartnerDashboardStats(session: StoredSession, activeRole: String): PartnerDashboardStats {
    ActiveRoleHolder.activeRole = activeRole
    val auth = ApiClient.bearer(session.accessToken)
    return try {
      if (!activeRole.startsWith("GURUJI")) {
        PartnerDashboardStats()
      } else {
        val entries = runCatching { api.myKnowledgeEntries(auth) }.getOrElse { emptyList() }
        val conflicts = runCatching { api.openConflicts(auth) }.getOrElse { emptyList() }
        val pendingReports = runCatching { api.gurujiPendingReports(auth) }.getOrElse { emptyList() }
        val bookings = runCatching { api.gurujiBookings(auth) }.getOrElse { emptyList() }
        val earnings = runCatching { api.earningsSummary(auth) }.getOrNull()
        val rating = runCatching { api.qualityRating(auth) }.getOrNull()
        PartnerDashboardStats(
          knowledgeEntries = entries.size,
          reportsPendingReview = pendingReports.size,
          openConflicts = conflicts.size,
          consultationBookings = bookings.size,
          monthlyEarningsInr = earnings?.totalEarnings ?: 0.0,
          qualityRating = rating?.averageRating,
          reviewCount = rating?.reviewCount ?: 0,
        )
      }
    } finally {
      ActiveRoleHolder.activeRole = null
    }
  }

  suspend fun bookConsultation(
    session: StoredSession,
    gurujiId: String,
    offer: ConsultationOffer,
    reportId: String? = null,
  ): BookingResponse {
    val scheduledAt = java.time.LocalDateTime.now()
      .plusDays(1)
      .withHour(10)
      .withMinute(0)
      .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    return api.bookConsultation(
      ApiClient.bearer(session.accessToken),
      BookConsultationRequest(
        gurujiId = gurujiId,
        consultationType = offer.apiType,
        scheduledAt = scheduledAt,
        reportId = reportId,
      ),
    )
  }

  suspend fun logout() = tokenStore.clear()

  private suspend fun persist(response: AuthResponse): StoredSession {
    val session = StoredSession(
      accessToken = response.accessToken,
      refreshToken = response.refreshToken,
      userId = response.userId,
      roles = response.roles,
    )
    tokenStore.save(session)
    return session
  }

  private fun mapError(e: HttpException): Exception {
    val body = e.response()?.errorBody()?.string()
    val message = try {
      val json = Gson().fromJson(body, JsonObject::class.java)
      json?.get("message")?.asString ?: json?.get("error")?.asString
    } catch (_: Exception) {
      null
    }
    return IllegalStateException(message ?: body ?: "HTTP ${e.code()}")
  }
}
