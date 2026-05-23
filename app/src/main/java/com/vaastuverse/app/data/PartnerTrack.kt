package com.vaastuverse.app.data

import com.vaastuverse.app.data.dto.ApplicationResponse
import com.vaastuverse.app.ui.partner.PartnerPersona

enum class PartnerTrack(val label: String, val emoji: String) {
    GURUJI("Guruji", "🕉"),
    DESIGNER("Designer", "🎨"),
    CHANNEL("Channel partner", "🤝"),
    ;

    companion object {
        fun fromRequestedRole(role: String): PartnerTrack? = when {
            role.startsWith("GURUJI") -> GURUJI
            role == "DESIGNER" -> DESIGNER
            role == "CHANNEL_PARTNER" -> CHANNEL
            else -> null
        }

        fun fromSession(session: StoredSession?): PartnerTrack? {
            val role = session?.partnerRoles?.firstOrNull()
                ?: session?.roles?.firstOrNull { it != "CUSTOMER" }
            return role?.let { fromRequestedRole(it) }
        }
    }
}

object PartnerAccess {
    fun isOnboarded(session: StoredSession?): Boolean =
        session?.hasPartnerRole == true

    fun hasOpenApplication(applications: List<ApplicationResponse>): Boolean =
        applications.any { it.status == "PENDING" || it.status == "UNDER_REVIEW" }

    /** Locked track if user is onboarded or has a non-rejected application. */
    fun lockedTrack(session: StoredSession?, applications: List<ApplicationResponse>): PartnerTrack? {
        PartnerTrack.fromSession(session)?.let { return it }
        return applications
            .filter { it.status == "PENDING" || it.status == "UNDER_REVIEW" || it.status == "APPROVED" }
            .mapNotNull { PartnerTrack.fromRequestedRole(it.requestedRole) }
            .firstOrNull()
    }

    fun canApplyForTrack(
        session: StoredSession?,
        applications: List<ApplicationResponse>,
        track: PartnerTrack,
    ): Boolean {
        if (isOnboarded(session)) return false
        val locked = lockedTrack(session, applications) ?: return true
        return locked == track
    }

    fun availableTracks(session: StoredSession?, applications: List<ApplicationResponse>): List<PartnerTrack> =
        PartnerTrack.entries.filter { canApplyForTrack(session, applications, it) }

    fun primaryPartnerRole(session: StoredSession?): String? =
        session?.partnerRoles?.firstOrNull()
            ?: session?.roles?.firstOrNull { it.startsWith("GURUJI") || it == "DESIGNER" || it == "CHANNEL_PARTNER" }

    fun partnerPersona(session: StoredSession?): PartnerPersona {
        return when (PartnerTrack.fromSession(session)) {
            PartnerTrack.GURUJI -> PartnerPersona.Guruji
            PartnerTrack.DESIGNER -> PartnerPersona.Designer
            PartnerTrack.CHANNEL -> PartnerPersona.Channel
            null -> PartnerPersona.Guruji
        }
    }

    fun onboardedTitle(session: StoredSession?): String {
        val track = PartnerTrack.fromSession(session)
        return if (track != null) "Partner (${track.label})" else "Partner"
    }

    fun partnerGateMessage(applications: List<ApplicationResponse>): String {
        val latest = applications
            .filter { it.status != "REJECTED" }
            .maxByOrNull { it.submittedAt.orEmpty() }
            ?: applications.maxByOrNull { it.submittedAt.orEmpty() }
        val track = latest?.requestedRole?.let { PartnerTrack.fromRequestedRole(it) }?.label ?: "partner"
        return when (latest?.status) {
            "PENDING" -> "Your $track application is waiting for review."
            "UNDER_REVIEW" -> "Changes were requested on your $track application. Address notes and resubmit."
            "REJECTED" -> "Your $track application was rejected. You may re-apply for the same partner type only."
            "APPROVED" -> "Your $track application is approved. Refresh session to open partner tools."
            else -> "Complete partner onboarding to unlock the partner experience."
        }
    }
}
