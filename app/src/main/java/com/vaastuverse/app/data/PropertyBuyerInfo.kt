package com.vaastuverse.app.data

data class PropertyBuyerInfo(
    val buyerDifferentFromUser: Boolean = false,
    val buyerFullName: String? = null,
    val buyerDateOfBirthIso: String? = null,
)

fun PropertyBuyerInfo.isValidForSubmission(profileHasDob: Boolean, userDobIso: String?): Boolean {
    if (buyerDifferentFromUser) {
        return !buyerFullName.isNullOrBlank() && !buyerDateOfBirthIso.isNullOrBlank()
    }
    return profileHasDob || !userDobIso.isNullOrBlank()
}
