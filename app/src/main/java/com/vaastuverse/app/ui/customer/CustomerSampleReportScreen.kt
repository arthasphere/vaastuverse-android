package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.VvType

@Composable
fun CustomerSampleReportScreen(
    useCase: CustomerUseCaseDetail,
    onBookFactoryConsultation: (() -> Unit)? = null,
) {
    val content = sampleReportContent(useCase.id)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "Template for clarity — your final report uses your floor plan and location.",
            style = VvType.body(11, VvColors.Ink3),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(VvColors.Ink)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                content.coverLabel,
                fontSize = 8.sp,
                letterSpacing = 1.2.sp,
                color = VvColors.Gold,
                fontWeight = FontWeight.Bold,
            )
            Text(content.propertyName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(content.location, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "${content.score}/100",
                    color = VvColors.Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(content.scoreLabel, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            }
            Text(
                "✓ Validated by Guruji",
                color = VvColors.Jade,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            content.zoneHeading,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = VvColors.Ink,
        )

        content.zonePoints.forEach { point ->
            ZoneFocusCard(point)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(VvColors.Ink)
                .padding(12.dp),
        ) {
            Text(
                content.verdict,
                fontStyle = FontStyle.Italic,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 16.sp,
            )
            Text(
                "— Guruji",
                fontSize = 9.sp,
                color = VvColors.Gold,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        if (useCase.id == CustomerUseCaseId.FACTORY && onBookFactoryConsultation != null) {
            Button(
                onClick = onBookFactoryConsultation,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = VvColors.Jade),
            ) {
                Text("Book Factory Consultation · ₹1,999 →")
            }
        }
    }
}

@Composable
private fun ZoneFocusCard(point: SampleZonePoint) {
    val bg = if (point.isWarning) Color(0xFFFFF0F0) else VvColors.JadeLight
    val border = if (point.isWarning) VvColors.Red.copy(alpha = 0.25f) else VvColors.Jade.copy(alpha = 0.25f)
    val titleColor = if (point.isWarning) VvColors.Red else VvColors.Jade
    val detailColor = if (point.isWarning) Color(0xFF5A1A1A) else Color(0xFF1A5045)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(9.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(point.icon, fontSize = 16.sp)
        Column {
            Text(point.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Text(point.detail, fontSize = 9.sp, color = detailColor, lineHeight = 14.sp)
        }
    }
}

private data class SampleZonePoint(
    val icon: String,
    val title: String,
    val detail: String,
    val isWarning: Boolean = false,
)

private data class SampleReportContent(
    val coverLabel: String,
    val propertyName: String,
    val location: String,
    val score: Int,
    val scoreLabel: String,
    val zoneHeading: String,
    val zonePoints: List<SampleZonePoint>,
    val verdict: String,
)

private fun sampleReportContent(id: CustomerUseCaseId): SampleReportContent = when (id) {
    CustomerUseCaseId.HOME -> SampleReportContent(
        coverLabel = "RESIDENTIAL VAASTU · SAMPLE",
        propertyName = "Priya Sharma · Flat 4B",
        location = "Sarjapur Road, Bengaluru",
        score = 74,
        scoreLabel = "Good · remedies recommended",
        zoneHeading = "Residential zone analysis",
        zonePoints = listOf(
            SampleZonePoint("✦", "Master bedroom — SW-adjacent (81/100)", "Supportive for rest and family harmony."),
            SampleZonePoint("⚠", "Kitchen & stove — SE (68/100)", "Stove–sink alignment needs remedy before possession.", isWarning = true),
            SampleZonePoint("✦", "Main entrance — NE (72/100)", "Strong first impression; keep foyer clutter-free."),
            SampleZonePoint("✓", "Living zone — centre (76/100)", "Good social energy; lighten south wall displays."),
        ),
        verdict = "The layout is fundamentally sound for a young family. The kitchen remedy is low-cost and can be done before possession.",
    )
    CustomerUseCaseId.OFFICE -> SampleReportContent(
        coverLabel = "COMMERCIAL VAASTU · SAMPLE",
        propertyName = "Ravi Kumar · Office",
        location = "Indiranagar, Bengaluru",
        score = 78,
        scoreLabel = "Good · one key adjustment",
        zoneHeading = "Office zone analysis",
        zonePoints = listOf(
            SampleZonePoint("✓", "MD / Owner cabin — SW (91/100)", "Excellent authority and decision-making position."),
            SampleZonePoint("✦", "Cash / finance — N (76/100)", "Accounts zone workable; strengthen shelving discipline."),
            SampleZonePoint("✦", "Main entrance — E (70/100)", "Client attraction good; improve reception seating orientation."),
            SampleZonePoint("✦", "Team zone — open plan (74/100)", "Productivity layout balanced for a 12-person team."),
        ),
        verdict = "Strong commercial layout — the reception tweak alone typically improves client walk-in quality.",
    )
    CustomerUseCaseId.SHOP -> SampleReportContent(
        coverLabel = "RETAIL VAASTU · SAMPLE",
        propertyName = "Lakshmi Sarees · Showroom",
        location = "Commercial Street, Bengaluru",
        score = 71,
        scoreLabel = "Good · footfall boost possible",
        zoneHeading = "Retail zone analysis",
        zonePoints = listOf(
            SampleZonePoint("✦", "Shop entrance (69/100)", "Signage strong; clear glass at door recommended."),
            SampleZonePoint("✦", "Cash counter — NE-adjacent (74/100)", "Billing zone supports daily cash flow."),
            SampleZonePoint("⚠", "Display aisle — centre-heavy (73/100)", "Lighten south wall displays for aisle flow.", isWarning = true),
            SampleZonePoint("✓", "Owner desk — rear sightline (77/100)", "Good visibility of shop floor."),
        ),
        verdict = "Entrance and billing fixes are quick wins before festive season footfall.",
    )
    CustomerUseCaseId.FACTORY -> SampleReportContent(
        coverLabel = "INDUSTRIAL VAASTU · SAMPLE",
        propertyName = "Kaveri Textiles Pvt Ltd",
        location = "KIADB Industrial Area, Dobbaspet, Bengaluru",
        score = 70,
        scoreLabel = "Good · key remedy needed",
        zoneHeading = "Industrial zone analysis",
        zonePoints = listOf(
            SampleZonePoint("✦", "Production zone — SE (88/100)", "Fire element zone. Machinery correctly placed. Production energy strong."),
            SampleZonePoint("✦", "Raw material storage — SW (84/100)", "Earth element stability. Correct placement prevents supply disruptions."),
            SampleZonePoint("⚠", "Dispatch / finished goods — NE (42/100)", "Finished goods should be in NW (Vayu zone) for movement. Current NE placement slows dispatch energy.", isWarning = true),
            SampleZonePoint("✓", "MD cabin — SW corner (91/100)", "Earth zone. Owner authority and stability. Excellent placement."),
        ),
        verdict = "The dispatch zone remedy is straightforward — relocate finished goods staging to the NW bay. I've seen this improve dispatch cycle times in similar Bengaluru factories.",
    )
}
