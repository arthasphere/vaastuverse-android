package com.vaastuverse.app.data

enum class AccountPersona {
    CUSTOMER,
    PARTNER,
    ;

    companion object {
        fun fromApi(value: String?): AccountPersona? = when (value?.uppercase()) {
            "CUSTOMER" -> CUSTOMER
            "PARTNER" -> PARTNER
            else -> null
        }
    }
}
