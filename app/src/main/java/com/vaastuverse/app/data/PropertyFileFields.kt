package com.vaastuverse.app.data

data class PropertyFileFieldSpec(
    val key: String,
    val label: String,
    val mimeTypes: List<String>,
)

object PropertyFileFields {
    val floorPlan = PropertyFileFieldSpec(
        key = PropertyFieldKeys.FLOOR_PLAN,
        label = "Floor Plan",
        mimeTypes = listOf("image/*", "application/pdf"),
    )

    val sitePlan = PropertyFileFieldSpec(
        key = PropertyFieldKeys.SITE_PLAN,
        label = "Site Plan",
        mimeTypes = listOf("image/*", "application/pdf"),
    )

    /** Optional uploads for every property type — customer may skip either. */
    fun specsFor(type: SavedPropertyType): List<PropertyFileFieldSpec> = listOf(floorPlan, sitePlan)

    fun isUploadedUrl(value: String): Boolean =
        value.startsWith("http://") || value.startsWith("https://")

    fun isLocalPending(value: String): Boolean =
        value.startsWith("local-upload://") || value.startsWith("content://") || value.startsWith("file://")
}
