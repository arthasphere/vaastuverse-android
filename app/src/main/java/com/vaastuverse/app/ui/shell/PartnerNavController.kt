package com.vaastuverse.app.ui.shell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class PartnerNavController {
    var openProfileRequest by mutableIntStateOf(0)
        private set

    var openSettingsRequest by mutableIntStateOf(0)
        private set

    fun openProfile() {
        openProfileRequest++
    }

    fun openSettings() {
        openSettingsRequest++
    }

    fun reset() {
        openProfileRequest = 0
        openSettingsRequest = 0
    }
}
