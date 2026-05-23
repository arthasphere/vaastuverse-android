package com.vaastuverse.app.data

import com.vaastuverse.app.ui.customer.CustomerUseCaseId
import java.util.UUID

enum class SavedPropertyType(val label: String, val icon: String) {
    HOME("Flat / Home", "🏠"),
    OFFICE("Office", "🏢"),
    SHOP("Shop", "🏪"),
    FACTORY("Factory", "🏭"),
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

data class SavedProperty(
    val id: String = UUID.randomUUID().toString(),
    val type: SavedPropertyType,
    val label: String,
    val address: String,
    val city: String? = null,
    val notes: String? = null,
)
