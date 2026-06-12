package com.vaastuverse.app.data.network

import com.vaastuverse.app.data.dto.AccountMeResponse
import com.vaastuverse.app.data.dto.ApplicationResponse
import com.vaastuverse.app.data.dto.ApplyRequest
import com.vaastuverse.app.data.dto.AuthResponse
import com.vaastuverse.app.data.dto.CustomerProfileRequest
import com.vaastuverse.app.data.dto.CustomerProfileResponse
import com.vaastuverse.app.data.dto.BookConsultationRequest
import com.vaastuverse.app.data.dto.LinkPropertyRequest
import com.vaastuverse.app.data.dto.CustomerOrderRequest
import com.vaastuverse.app.data.dto.CustomerOrderResponse
import com.vaastuverse.app.data.dto.ReportAccessUrlResponse
import com.vaastuverse.app.data.dto.CustomerPropertyRequest
import com.vaastuverse.app.data.dto.CustomerPropertyResponse
import com.vaastuverse.app.data.dto.GuruMatchingFeaturesResponse
import com.vaastuverse.app.data.dto.GuruRatingResponse
import com.vaastuverse.app.data.dto.SubmitGuruRatingRequest
import com.vaastuverse.app.data.dto.DeliverReportRequest
import com.vaastuverse.app.data.dto.LoginRequest
import com.vaastuverse.app.data.dto.MessageResponse
import com.vaastuverse.app.data.dto.OtpSendRequest
import com.vaastuverse.app.data.dto.OtpVerifyRequest
import com.vaastuverse.app.data.dto.SetPersonaRequest
import com.vaastuverse.app.data.dto.BookingResponse
import com.vaastuverse.app.data.dto.ConflictCaseResponse
import com.vaastuverse.app.data.dto.DiscoverablePartnerResponse
import com.vaastuverse.app.data.dto.EarningsSummaryResponse
import com.vaastuverse.app.data.dto.KnowledgeEntryResponse
import com.vaastuverse.app.data.dto.PartnerProfileResponse
import com.vaastuverse.app.data.dto.QualityRatingResponse
import com.vaastuverse.app.data.dto.RegisterRequest
import com.vaastuverse.app.data.dto.GurujiAvailabilityResponse
import com.vaastuverse.app.data.dto.ReportSummaryResponse
import com.vaastuverse.app.data.dto.ReportWorkflowResponse
import com.vaastuverse.app.data.dto.SubmitReportFeedbackRequest
import com.vaastuverse.app.data.dto.SubmitReportReviewRequest
import com.vaastuverse.app.data.dto.TwoWeekAvailabilityRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

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

    @POST("api/v1/auth/persona")
    suspend fun setPersona(
        @Header("Authorization") authorization: String,
        @Body body: SetPersonaRequest,
    ): AccountMeResponse

    @DELETE("api/v1/auth/account")
    suspend fun deleteAccount(@Header("Authorization") authorization: String): MessageResponse

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

    @GET("api/v1/users/{userId}/properties")
    suspend fun listCustomerProperties(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
        @Query("type") type: String? = null,
    ): List<CustomerPropertyResponse>

    @GET("api/v1/users/{userId}/properties/{propertyId}")
    suspend fun getCustomerProperty(
        @Path("userId") userId: String,
        @Path("propertyId") propertyId: String,
        @Header("Authorization") authorization: String,
    ): CustomerPropertyResponse

    @POST("api/v1/users/{userId}/properties")
    suspend fun createCustomerProperty(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
        @Body body: CustomerPropertyRequest,
    ): CustomerPropertyResponse

    @PUT("api/v1/users/{userId}/properties/{propertyId}")
    suspend fun updateCustomerProperty(
        @Path("userId") userId: String,
        @Path("propertyId") propertyId: String,
        @Header("Authorization") authorization: String,
        @Body body: CustomerPropertyRequest,
    ): CustomerPropertyResponse

    @DELETE("api/v1/users/{userId}/properties/{propertyId}")
    suspend fun deleteCustomerProperty(
        @Path("userId") userId: String,
        @Path("propertyId") propertyId: String,
        @Header("Authorization") authorization: String,
    )

    @GET("api/v1/users/{userId}/orders")
    suspend fun listCustomerOrders(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
    ): List<CustomerOrderResponse>

    @POST("api/v1/users/{userId}/orders")
    suspend fun createCustomerOrder(
        @Path("userId") userId: String,
        @Header("Authorization") authorization: String,
        @Body body: CustomerOrderRequest,
    ): CustomerOrderResponse

    @PUT("api/v1/users/{userId}/orders/{orderId}")
    suspend fun updateCustomerOrder(
        @Path("userId") userId: String,
        @Path("orderId") orderId: String,
        @Header("Authorization") authorization: String,
        @Body body: CustomerOrderRequest,
    ): CustomerOrderResponse

    @POST("api/v1/users/{userId}/orders/{orderId}/link-property")
    suspend fun linkPropertyToOrder(
        @Path("userId") userId: String,
        @Path("orderId") orderId: String,
        @Header("Authorization") authorization: String,
        @Body body: LinkPropertyRequest,
    ): CustomerOrderResponse

    @GET("api/v1/users/{userId}/orders/{orderId}/report-url")
    suspend fun getOrderReportUrl(
        @Path("userId") userId: String,
        @Path("orderId") orderId: String,
        @Query("mode") mode: String,
        @Header("Authorization") authorization: String,
    ): ReportAccessUrlResponse

    @POST("api/v1/users/{userId}/orders/{orderId}/rating")
    suspend fun submitGuruRating(
        @Path("userId") userId: String,
        @Path("orderId") orderId: String,
        @Header("Authorization") authorization: String,
        @Body body: SubmitGuruRatingRequest,
    ): GuruRatingResponse

    @GET("api/v1/users/{userId}/orders/{orderId}/rating")
    suspend fun getGuruRating(
        @Path("userId") userId: String,
        @Path("orderId") orderId: String,
        @Header("Authorization") authorization: String,
    ): GuruRatingResponse?

    @GET("api/v1/features/guru-matching")
    suspend fun getGuruMatchingFeatures(): GuruMatchingFeaturesResponse

    @POST("api/v1/users/{userId}/orders/{orderId}/upgrade-to-guruji")
    suspend fun upgradeOrderToGuruji(
        @Path("userId") userId: String,
        @Path("orderId") orderId: String,
        @Header("Authorization") authorization: String,
    ): CustomerOrderResponse

    @GET("api/v1/guruji/orders/pending")
    suspend fun listGurujiPendingOrders(
        @Header("Authorization") authorization: String,
    ): List<CustomerOrderResponse>

    @GET("api/v1/guruji/orders/assigned")
    suspend fun listGurujiAssignedOrders(
        @Header("Authorization") authorization: String,
    ): List<CustomerOrderResponse>

    @GET("api/v1/guruji/orders/consultations")
    suspend fun listGurujiConsultationOrders(
        @Header("Authorization") authorization: String,
    ): List<CustomerOrderResponse>

    @POST("api/v1/guruji/orders/{orderId}/deliver")
    suspend fun deliverOrderReport(
        @Path("orderId") orderId: String,
        @Header("Authorization") authorization: String,
        @Body body: DeliverReportRequest,
    ): CustomerOrderResponse

    @POST("api/v1/consultations/book")
    suspend fun bookConsultation(
        @Header("Authorization") authorization: String,
        @Body body: BookConsultationRequest,
    ): BookingResponse

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

    @GET("api/v1/consultations/guruji/availability/two-weeks")
    suspend fun getGurujiTwoWeekAvailability(
        @Header("Authorization") authorization: String,
    ): List<GurujiAvailabilityResponse>

    @PUT("api/v1/consultations/guruji/availability/two-weeks")
    suspend fun saveGurujiTwoWeekAvailability(
        @Header("Authorization") authorization: String,
        @Body body: TwoWeekAvailabilityRequest,
    ): List<GurujiAvailabilityResponse>

    @GET("api/vastu/reports/by-order/{orderId}/workflow")
    suspend fun getReportWorkflowByOrder(
        @Path("orderId") orderId: String,
        @Header("Authorization") authorization: String,
    ): ReportWorkflowResponse

    @GET("api/vastu/reports/{reportId}/workflow")
    suspend fun getReportWorkflow(
        @Path("reportId") reportId: String,
        @Header("Authorization") authorization: String,
    ): ReportWorkflowResponse

    @POST("api/vastu/reports/{reportId}/feedback")
    suspend fun submitReportFeedback(
        @Path("reportId") reportId: String,
        @Header("Authorization") authorization: String,
        @Body body: SubmitReportFeedbackRequest,
    ): ReportWorkflowResponse

    @POST("api/vastu/reports/{reportId}/submit")
    suspend fun submitReportForReview(
        @Path("reportId") reportId: String,
        @Header("Authorization") authorization: String,
        @Body body: SubmitReportReviewRequest? = null,
    ): ReportWorkflowResponse
}
