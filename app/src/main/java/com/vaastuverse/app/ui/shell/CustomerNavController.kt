package com.vaastuverse.app.ui.shell

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CustomerNavController {
    var title by mutableStateOf("")
        private set

    var showBack by mutableStateOf(false)
        private set

    var onBack: () -> Unit = {}
        private set

    var openProfileRequest by mutableStateOf(0)
        private set

    var openPropertiesRequest by mutableStateOf(0)
        private set

    var openSettingsRequest by mutableIntStateOf(0)
        private set

    fun update(title: String, showBack: Boolean, onBack: () -> Unit = {}) {
        this.title = title
        this.showBack = showBack
        this.onBack = onBack
    }

    fun reset() {
        update(title = "", showBack = false, onBack = {})
    }

    fun openProfile() {
        openProfileRequest++
    }

    fun openProperties() {
        openPropertiesRequest++
    }

    fun openSettings() {
        openSettingsRequest++
    }
}
