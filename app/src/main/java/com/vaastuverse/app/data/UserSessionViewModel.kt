package com.vaastuverse.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.vaastuverse.app.ui.partner.PartnerPersona

class UserSessionViewModel : ViewModel() {
    var displayName by mutableStateOf("Priya Sharma")
        private set

    var organizationName by mutableStateOf<String?>(null)
        private set

    var leadExpertDisplayName by mutableStateOf("Shruti Gupta")
        private set

    val givenName: String
        get() = displayName.trim().split(" ").firstOrNull() ?: displayName

    val namasteGreetingLine: String
        get() = "NAMASTE, ${givenName.uppercase()} JI 🙏"

    val morningGreetingLine: String
        get() = "Good morning, $givenName"

    val designerPartnerHeroSubtitle: String
        get() {
            val org = organizationName?.trim().takeUnless { it.isNullOrEmpty() } ?: "Your studio"
            return "${org.uppercase()} · DESIGNER PARTNER"
        }

    val channelPartnerHeroSubtitle: String
        get() {
            val org = organizationName?.trim().takeUnless { it.isNullOrEmpty() } ?: "Your business"
            return "${org.uppercase()} · CHANNEL PARTNER"
        }

    val leadGuideShortName: String
        get() = givenNameFrom(leadExpertDisplayName)

    val leadExpertPossessivePhrase: String
        get() = "${leadGuideShortName}'s"

    fun applyProfile(
        displayName: String,
        organizationName: String? = null,
        leadExpertDisplayName: String? = null,
    ) {
        this.displayName = displayName
        this.organizationName = organizationName
        if (!leadExpertDisplayName.isNullOrBlank()) {
            this.leadExpertDisplayName = leadExpertDisplayName
        }
    }

    fun applyPartnerDevProfile(persona: PartnerPersona) {
        when (persona) {
            PartnerPersona.Guruji -> applyProfile("Shruti Gupta")
            PartnerPersona.Designer -> applyProfile("Kavita", organizationName = "Kavita Interiors")
            PartnerPersona.Channel -> applyProfile("Rahul", organizationName = "Rahul Realty")
        }
    }

    private fun givenNameFrom(fullName: String): String {
        val t = fullName.trim()
        return t.split(" ").firstOrNull() ?: t
    }
}
