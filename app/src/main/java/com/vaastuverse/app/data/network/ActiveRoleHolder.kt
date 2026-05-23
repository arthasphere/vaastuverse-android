package com.vaastuverse.app.data.network

/** Thread-local active role for gateway `X-Active-Role` (partner API calls). */
object ActiveRoleHolder {
    @Volatile
    var activeRole: String? = null
}
