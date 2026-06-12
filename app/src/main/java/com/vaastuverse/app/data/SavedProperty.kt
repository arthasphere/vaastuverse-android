package com.vaastuverse.app.data

import com.vaastuverse.app.ui.customer.CustomerUseCaseId
import java.util.UUID

enum class SavedPropertyType(val label: String, val icon: String) {
    HOME("New Flat / Home", "🏠"),
    OFFICE("Office / Workspace", "🏢"),
    SHOP("Shop / Showroom", "🏪"),
    FACTORY("Factory / Warehouse", "🏭"),
    ;

    fun toUseCaseId(): CustomerUseCaseId = when (this) {
        HOME -> CustomerUseCaseId.HOME
        OFFICE -> CustomerUseCaseId.OFFICE
        SHOP -> CustomerUseCaseId.SHOP
        FACTORY -> CustomerUseCaseId.FACTORY
    }

    companion object {
        fun fromUseCaseId(id: CustomerUseCaseId): SavedPropertyType = when (id) {
            CustomerUseCaseId.HOME -> HOME
            CustomerUseCaseId.OFFICE -> OFFICE
            CustomerUseCaseId.SHOP -> SHOP
            CustomerUseCaseId.FACTORY -> FACTORY
        }
    }
}

fun newLocalPropertyId(): String = "local-${UUID.randomUUID()}"

fun SavedProperty.isLocalOnly(): Boolean = id.startsWith("local-")

data class SavedProperty(
    val id: String = newLocalPropertyId(),
    val type: SavedPropertyType,
    val label: String,
    val details: Map<String, String> = emptyMap(),
    // Legacy fields kept for local migration only
    val address: String? = null,
    val city: String? = null,
    val notes: String? = null,
)
