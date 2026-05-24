package com.vaastuverse.app.data.network

import com.vaastuverse.app.data.dto.AccountMeResponse
import com.vaastuverse.app.data.dto.ApplicationResponse
import com.vaastuverse.app.data.dto.ApplyRequest
import com.vaastuverse.app.data.dto.AuthResponse
import com.vaastuverse.app.data.dto.CustomerProfileRequest
import com.vaastuverse.app.data.dto.CustomerProfileResponse
import com.vaastuverse.app.data.dto.LoginRequest
import com.vaastuverse.app.data.dto.MessageResponse
import com.vaastuverse.app.data.dto.OtpSendRequest
import com.vaastuverse.app.data.dto.OtpVerifyRequest
import com.vaastuverse.app.data.dto.BookingResponse
import com.vaastuverse.app.data.dto.ConflictCaseResponse
import com.vaastuverse.app.data.dto.DiscoverablePartnerResponse
import com.vaastuverse.app.data.dto.EarningsSummaryResponse
import com.vaastuverse.app.data.dto.KnowledgeEntryResponse
import com.vaastuverse.app.data.dto.PartnerProfileResponse
import com.vaastuverse.app.data.dto.QualityRatingResponse
import com.vaastuverse.app.data.dto.RegisterRequest
import com.vaastuverse.app.data.dto.ReportSummaryResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface VaastuVerseApi {
    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("api/v1/auth/otp/send")
    suspend fun sendOtp(@Body body: OtpSendRequest): MessageResponse

    @POST("api/v1/auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyRequest): AuthResponse

    @POST("api/v1/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Header("X-Refresh-Token") refreshToken: String): AuthResponse

    @GET("api/v1/auth/me")
    suspend fun getAccountMe(@Header("Authorization") authorization: String): AccountMeResponse

    @GET("api/v1/users/{userId}/customer-profile")
    suspend fun getCustomerProfile(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
    ): CustomerProfileResponse

    @POST("api/v1/users/{userId}/customer-profile")
    suspend fun createCustomerProfile(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
        @Body body: CustomerProfileRequest,
    ): CustomerProfileResponse

    @PUT("api/v1/users/{userId}/customer-profile")
    suspend fun updateCustomerProfile(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
        @Body body: CustomerProfileRequest,
    ): CustomerProfileResponse

    @GET("api/v1/users/{userId}/partner-profiles")
    suspend fun listPartnerProfiles(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
    ): List<PartnerProfileResponse>

    @POST("api/v1/onboarding/apply")
    suspend fun applyOnboarding(
        @Header("Authorization") authorization: String,
        @Body body: ApplyRequest,
    ): ApplicationResponse

    @GET("api/v1/onboarding/my")
    suspend fun myOnboardingApplications(
        @Header("Authorization") authorization: String,
    ): List<ApplicationResponse>

    @GET("api/v1/users/partners/discover")
    suspend fun discoverPartners(
        @Header("Authorization") authorization: String,
        @retrofit2.http.Query("type") type: String = "ALL",
    ): List<DiscoverablePartnerResponse>

    @GET("api/v1/knowledge/entries/my")
    suspend fun myKnowledgeEntries(
        @Header("Authorization") authorization: String,
    ): List<KnowledgeEntryResponse>

    @GET("api/v1/knowledge/conflicts")
    suspend fun openConflicts(
        @Header("Authorization") authorization: String,
    ): List<ConflictCaseResponse>

    @GET("api/v1/reports/guruji/pending")
    suspend fun gurujiPendingReports(
        @Header("Authorization") authorization: String,
    ): List<ReportSummaryResponse>

    @GET("api/v1/earnings/summary")
    suspend fun earningsSummary(
        @Header("Authorization") authorization: String,
    ): EarningsSummaryResponse

    @GET("api/v1/earnings/rating")
    suspend fun qualityRating(
        @Header("Authorization") authorization: String,
    ): QualityRatingResponse

    @GET("api/v1/consultations/guruji/bookings")
    suspend fun gurujiBookings(
        @Header("Authorization") authorization: String,
    ): List<BookingResponse>
}
