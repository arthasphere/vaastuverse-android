package com.vaastuverse.app.data.repository

import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.dto.FeedbackItemRequest
import com.vaastuverse.app.data.dto.ReportWorkflowResponse
import com.vaastuverse.app.data.dto.SubmitReportFeedbackRequest
import com.vaastuverse.app.data.network.ApiClient

class ReportReviewRepository {
    private val api get() = ApiClient.api

    suspend fun loadWorkflowByOrder(session: StoredSession, orderId: String): ReportWorkflowResponse? {
        val auth = ApiClient.bearer(session.accessToken)
        return try {
            api.getReportWorkflowByOrder(orderId, auth)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun loadWorkflow(session: StoredSession, reportId: String): ReportWorkflowResponse? {
        return try {
            api.getReportWorkflow(reportId, ApiClient.bearer(session.accessToken))
        } catch (_: Exception) {
            null
        }
    }

    suspend fun submitFeedback(
        session: StoredSession,
        reportId: String,
        sectionTag: String,
        note: String,
    ): ReportWorkflowResponse {
        return api.submitReportFeedback(
            reportId,
            ApiClient.bearer(session.accessToken),
            SubmitReportFeedbackRequest(
                items = listOf(
                    FeedbackItemRequest(
                        sectionTag = sectionTag,
                        note = note,
                        createdBy = session.userId,
                    ),
                ),
                reviewerId = session.userId,
            ),
        )
    }

    suspend fun submitForReview(session: StoredSession, reportId: String): ReportWorkflowResponse {
        return api.submitReportForReview(
            reportId,
            ApiClient.bearer(session.accessToken),
            null,
        )
    }
}
