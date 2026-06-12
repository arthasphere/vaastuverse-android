package com.vaastuverse.app.ui.partner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.dto.ReportWorkflowResponse
import com.vaastuverse.app.data.repository.OrderRepository
import com.vaastuverse.app.data.repository.ReportReviewRepository
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType
import com.vaastuverse.app.ui.customer.CustomerUseCases
import kotlinx.coroutines.launch

@Composable
fun PartnerGurujiReportsScreen(
    session: StoredSession?,
    orderRepo: OrderRepository,
    reviewRepo: ReportReviewRepository = remember { ReportReviewRepository() },
    modifier: Modifier = Modifier,
    onNotify: (String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var orders by remember { mutableStateOf(emptyList<CustomerOrder>()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun reload() {
        val current = session ?: return
        scope.launch {
            loading = true
            error = null
            runCatching {
                orders = orderRepo.listGurujiAssignedReportOrders(current)
            }.onFailure {
                error = it.message ?: "Could not load assigned reports"
            }
            loading = false
        }
    }

    LaunchedEffect(session?.userId) { reload() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "REPORTS TO REVIEW",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = VvColors.Ink3,
            letterSpacing = 1.sp,
        )
        Text(
            "Review the generated report, add feedback, and submit for approval. You do not upload PDFs here.",
            style = VvType.body(11, VvColors.Ink3),
        )

        when {
            loading -> CircularProgressIndicator(color = VvColors.Saffron)
            error != null -> Text(error!!, color = VvColors.Red, fontSize = 12.sp)
            orders.isEmpty() -> Text("No reports assigned for review right now.", style = VvType.body(12, VvColors.Ink3))
            else -> orders.forEach { order ->
                GurujiReportReviewCard(
                    order = order,
                    session = session,
                    reviewRepo = reviewRepo,
                    onUpdated = {
                        onNotify("Report review updated")
                        reload()
                    },
                    onError = { message -> error = message },
                )
            }
        }
    }
}

@Composable
private fun GurujiReportReviewCard(
    order: CustomerOrder,
    session: StoredSession?,
    reviewRepo: ReportReviewRepository,
    onUpdated: () -> Unit,
    onError: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val useCase = CustomerUseCases.get(order.useCaseId)
    var workflow by remember(order.id) { mutableStateOf<ReportWorkflowResponse?>(null) }
    var loadingWorkflow by remember(order.id) { mutableStateOf(true) }
    var sectionTag by remember(order.id) { mutableStateOf("general") }
    var feedbackNote by remember(order.id) { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    LaunchedEffect(order.id, session?.userId) {
        val current = session ?: return@LaunchedEffect
        loadingWorkflow = true
        workflow = reviewRepo.loadWorkflowByOrder(current, order.id)
            ?: order.publishedReportId?.let { reportId ->
                reviewRepo.loadWorkflow(current, reportId)
            }
        loadingWorkflow = false
    }

    val reportId = workflow?.reportId ?: order.publishedReportId
    val versionStatus = workflow?.currentVersion?.status ?: order.reportStatus ?: "pending"
    val feedbackCount = workflow?.currentVersion?.feedbackItems?.orEmpty()?.size ?: 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("${useCase.icon} ${order.packageTitle}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        order.propertyLabel?.takeIf { it.isNotBlank() }?.let {
            Text(it, fontSize = 11.sp, color = VvColors.Ink3)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Status: $versionStatus", fontSize = 10.sp, color = VvColors.Jade)
            Text("Feedback items: $feedbackCount", fontSize = 10.sp, color = VvColors.Ink3)
        }

        if (loadingWorkflow) {
            CircularProgressIndicator(color = VvColors.Saffron, strokeWidth = 2.dp)
        } else if (reportId.isNullOrBlank()) {
            Text(
                "Report draft is being prepared. You will be able to review once the system links a report to this order.",
                style = VvType.body(11, VvColors.Ink3),
            )
        } else {
            workflow?.currentVersion?.feedbackItems.orEmpty().takeLast(3).forEach { item ->
                Text(
                    "· ${item.sectionTag}: ${item.note}",
                    fontSize = 10.sp,
                    color = VvColors.Ink2,
                )
            }

            OutlinedTextField(
                value = sectionTag,
                onValueChange = { sectionTag = it },
                label = { Text("Section tag") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = feedbackNote,
                onValueChange = { feedbackNote = it },
                label = { Text("Feedback note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val current = session ?: return@Button
                        if (feedbackNote.isBlank()) {
                            onError("Enter feedback before saving")
                            return@Button
                        }
                        scope.launch {
                            submitting = true
                            runCatching {
                                workflow = reviewRepo.submitFeedback(
                                    current,
                                    reportId,
                                    sectionTag.trim().ifBlank { "general" },
                                    feedbackNote.trim(),
                                )
                                feedbackNote = ""
                                onUpdated()
                            }.onFailure {
                                onError(it.message ?: "Could not save feedback")
                            }
                            submitting = false
                        }
                    },
                    enabled = !submitting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add feedback")
                }
                Button(
                    onClick = {
                        val current = session ?: return@Button
                        scope.launch {
                            submitting = true
                            runCatching {
                                workflow = reviewRepo.submitForReview(current, reportId)
                                onUpdated()
                            }.onFailure {
                                onError(it.message ?: "Could not submit for review")
                            }
                            submitting = false
                        }
                    },
                    enabled = !submitting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Submit review")
                }
            }
        }
    }
}
