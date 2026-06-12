package com.vaastuverse.app.data

import com.vaastuverse.app.data.dto.GuruMatchingFeaturesResponse

/**
 * Cached guru-matching feature flags from backend.
 * Defaults keep AI + upgrade flows hidden until the server enables them.
 */
object FeatureFlags {
    @Volatile
    var guruMatching: GuruMatchingFeaturesResponse = GuruMatchingFeaturesResponse()
        private set

    fun updateGuruMatching(features: GuruMatchingFeaturesResponse) {
        guruMatching = features
    }

    val aiGuruEnabled: Boolean get() = guruMatching.aiGuruEnabled
    val upgradePlanEnabled: Boolean get() = guruMatching.upgradePlanEnabled
}
