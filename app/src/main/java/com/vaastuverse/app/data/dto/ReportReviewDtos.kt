package com.vaastuverse.app.data.dto

data class ReportWorkflowResponse(
    val reportId: String? = null,
    val orderId: String? = null,
    val currentVersionNumber: Int? = null,
    val publishedVersionNumber: Int? = null,
    val locked: Boolean? = null,
    val currentVersion: ReportVersionResponse? = null,
)

data class ReportVersionResponse(
    val versionNumber: Int? = null,
    val status: String? = null,
    val feedbackItems: List<FeedbackItemResponse>? = null,
)

data class FeedbackItemResponse(
    val id: String? = null,
    val sectionTag: String? = null,
    val note: String? = null,
    val type: String? = null,
    val included: Boolean? = null,
)

data class SubmitReportFeedbackRequest(
    val items: List<FeedbackItemRequest>,
    val reviewerId: String? = null,
)

data class FeedbackItemRequest(
    val sectionTag: String,
    val note: String,
    val type: String = "COMMENT",
    val included: Boolean = true,
    val createdBy: String? = null,
)

data class SubmitReportReviewRequest(
    val note: String? = null,
)
