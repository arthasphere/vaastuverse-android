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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.StoredSession
import com.vaastuverse.app.data.dto.AvailabilitySlotRequest
import com.vaastuverse.app.data.dto.BookingResponse
import com.vaastuverse.app.data.repository.ConsultationRepository
import com.vaastuverse.app.data.repository.OrderRepository
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class DayAvailabilityDraft(
    val date: LocalDate,
    val enabled: Boolean = false,
    val startTime: String = "10:00",
    val endTime: String = "18:00",
)

@Composable
fun PartnerGurujiConsultationsScreen(
    session: StoredSession?,
    orderRepo: OrderRepository,
    consultationRepo: ConsultationRepository = remember { ConsultationRepository() },
    modifier: Modifier = Modifier,
    onNotify: (String) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var consultationOrders by remember { mutableStateOf(emptyList<CustomerOrder>()) }
    var serviceBookings by remember { mutableStateOf(emptyList<BookingResponse>()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val dayDrafts = remember { mutableStateMapOf<String, DayAvailabilityDraft>() }

    fun buildDayDrafts(existing: List<com.vaastuverse.app.data.dto.GurujiAvailabilityResponse>) {
        dayDrafts.clear()
        val today = LocalDate.now()
        repeat(14) { offset ->
            val date = today.plusDays(offset.toLong())
            val key = date.toString()
            val match = existing.firstOrNull { it.slotDate == key }
            dayDrafts[key] = DayAvailabilityDraft(
                date = date,
                enabled = match != null,
                startTime = match?.startTime ?: "10:00",
                endTime = match?.endTime ?: "18:00",
            )
        }
    }

    fun reload() {
        val current = session ?: return
        scope.launch {
            loading = true
            error = null
            runCatching {
                consultationOrders = orderRepo.listGurujiConsultationOrders(current)
                serviceBookings = consultationRepo.listGurujiBookings(current)
                buildDayDrafts(consultationRepo.getTwoWeekAvailability(current))
            }.onFailure {
                error = it.message ?: "Could not load consultations"
            }
            loading = false
        }
    }

    LaunchedEffect(session?.userId) { reload() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "CONSULTATIONS",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = VvColors.Ink3,
            letterSpacing = 1.sp,
        )
        Text(
            "See bookings linked to your reports and set your availability for the next 2 weeks.",
            style = VvType.body(11, VvColors.Ink3),
        )

        when {
            loading -> CircularProgressIndicator(color = VvColors.Saffron)
            error != null -> Text(error!!, color = VvColors.Red, fontSize = 12.sp)
        }

        if (!loading) {
            Text("BOOKED WITH YOU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = VvColors.Ink2)
            val allBookings = consultationOrders.size + serviceBookings.size
            if (allBookings == 0) {
                Text("No consultation bookings yet.", style = VvType.body(12, VvColors.Ink3))
            } else {
                consultationOrders.forEach { order ->
                    ConsultationBookingCard(
                        title = order.packageTitle,
                        subtitle = order.propertyLabel ?: "Customer consultation",
                        meta = "${order.priceLabel} · ${order.status}",
                    )
                }
                serviceBookings.forEach { booking ->
                    ConsultationBookingCard(
                        title = booking.consultationType ?: "Consultation",
                        subtitle = "Customer ${booking.customerId?.take(8) ?: "—"}",
                        meta = listOfNotNull(
                            booking.durationMinutes?.let { "${it} min" },
                            booking.scheduledAt,
                            booking.status,
                        ).joinToString(" · "),
                    )
                }
            }

            Text(
                "AVAILABILITY — NEXT 2 WEEKS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = VvColors.Ink2,
                modifier = Modifier.padding(top = 4.dp),
            )
            dayDrafts.values.sortedBy { it.date }.forEach { initial ->
                val key = initial.date.toString()
                val draft = dayDrafts[key] ?: initial
                AvailabilityDayRow(
                    draft = draft,
                    onToggle = { enabled ->
                        val current = dayDrafts[key] ?: draft
                        dayDrafts[key] = current.copy(enabled = enabled)
                    },
                    onStartChange = { start ->
                        val current = dayDrafts[key] ?: draft
                        dayDrafts[key] = current.copy(startTime = start)
                    },
                    onEndChange = { end ->
                        val current = dayDrafts[key] ?: draft
                        dayDrafts[key] = current.copy(endTime = end)
                    },
                )
            }
            Button(
                onClick = {
                    val current = session ?: return@Button
                    scope.launch {
                        saving = true
                        runCatching {
                            val slots = dayDrafts.values
                                .filter { it.enabled }
                                .map {
                                    AvailabilitySlotRequest(
                                        date = it.date.toString(),
                                        startTime = it.startTime,
                                        endTime = it.endTime,
                                    )
                                }
                            consultationRepo.saveTwoWeekAvailability(current, slots)
                            onNotify("Availability saved for next 2 weeks")
                            reload()
                        }.onFailure {
                            error = it.message ?: "Could not save availability"
                        }
                        saving = false
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (saving) "Saving…" else "Save availability")
            }
        }
    }
}

@Composable
private fun ConsultationBookingCard(
    title: String,
    subtitle: String,
    meta: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(subtitle, fontSize = 11.sp, color = VvColors.Ink3)
        Text(meta, fontSize = 10.sp, color = VvColors.Saffron)
    }
}

@Composable
private fun AvailabilityDayRow(
    draft: DayAvailabilityDraft,
    onToggle: (Boolean) -> Unit,
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
) {
    val label = draft.date.format(DateTimeFormatter.ofPattern("EEE, d MMM", Locale.getDefault()))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, VvColors.Border, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Switch(checked = draft.enabled, onCheckedChange = onToggle)
        }
        if (draft.enabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.startTime,
                    onValueChange = onStartChange,
                    label = { Text("From") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = draft.endTime,
                    onValueChange = onEndChange,
                    label = { Text("To") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
            }
        }
    }
}
