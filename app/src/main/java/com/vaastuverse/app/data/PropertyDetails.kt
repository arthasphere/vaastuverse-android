package com.vaastuverse.app.data

object PropertyFieldKeys {    const val PROJECT_NAME = "projectName"
    const val TOWER_NAME = "towerName"
    const val FLAT_NUMBER = "flatNumber"
    const val CONFIGURATION = "configuration"
    const val TOWER_FACING = "towerFacing"
    const val MOVE_IN_TIMELINE = "moveInTimeline"
    const val FLOOR_PLAN = "floorPlan"
    const val SITE_PLAN = "sitePlan"
    const val EXACT_ADDRESS = "exactAddress"
    const val INTERIOR_LAYOUT_NOTES = "interiorLayoutNotes"

    const val BUSINESS_NAME = "businessName"
    const val OFFICE_ADDRESS = "officeAddress"
    const val OFFICE_TYPE = "officeType"
    const val TOTAL_AREA_SQ_FT = "totalAreaSqFt"
    const val KEY_ZONES = "keyZones"
    const val FACING_DIRECTION = "facingDirection"
    const val TEAM_SIZE = "teamSize"
    const val OFFICE_BUSINESS_TYPE = "officeBusinessType"

    const val SHOP_NAME = "shopName"
    const val LOCATION = "location"
    const val SIZE_CATEGORY = "sizeCategory"
    const val BUSINESS_TYPE = "businessType"
    const val MAIN_ENTRANCE_FACING = "mainEntranceFacing"
    const val COUNTER_LOCATION = "counterLocation"
    const val PRODUCT_CATEGORY = "productCategory"
    const val FOOTFALL_PATTERN = "footfallPattern"

    const val FACILITY_NAME = "facilityName"
    const val FACILITY_TYPE = "facilityType"
    const val MACHINERY_ZONES = "machineryZones"
    const val DISPATCH_AREA = "dispatchArea"
    const val SITE_ORIENTATION = "siteOrientation"
    const val PROCESS_FLOW = "processFlow"
    const val EMPLOYEE_COUNT = "employeeCount"
}

val CompassDirections = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
val BhkOptions = listOf("1 BHK", "2 BHK", "3 BHK", "4 BHK")
val OfficeTypeOptions = listOf("Individual cabin", "Shared", "Full floor")
val ShopSizeOptions = listOf("Small", "Medium", "Large")
val ShopBusinessTypeOptions = listOf("Retail", "Services")
val FactoryTypeOptions = listOf("Manufacturing", "Warehouse", "Storage")
val OfficeBusinessTypeOptions = listOf("Services", "Retail", "Manufacturing")

fun SavedProperty.toDetailsMap(): Map<String, String> = details.filterValues { it.isNotBlank() }

fun SavedProperty.hasPendingFileUploads(): Boolean =
    details.values.any { PropertyFileFields.isLocalPending(it) }

fun SavedProperty.missingRequiredFieldLabels(): List<String> {
    val required = when (type) {
        SavedPropertyType.HOME -> listOf(
            PropertyFieldKeys.PROJECT_NAME to "Project name",
            PropertyFieldKeys.TOWER_NAME to "Tower name",
            PropertyFieldKeys.FLAT_NUMBER to "Flat number",
            PropertyFieldKeys.CONFIGURATION to "Configuration",
            PropertyFieldKeys.TOWER_FACING to "Tower facing",
            PropertyFieldKeys.MOVE_IN_TIMELINE to "Move-in timeline",
        )
        SavedPropertyType.OFFICE -> listOf(
            PropertyFieldKeys.BUSINESS_NAME to "Business name",
            PropertyFieldKeys.OFFICE_ADDRESS to "Office address",
            PropertyFieldKeys.OFFICE_TYPE to "Office type",
            PropertyFieldKeys.TOTAL_AREA_SQ_FT to "Total area",
            PropertyFieldKeys.KEY_ZONES to "Key zones",
            PropertyFieldKeys.FACING_DIRECTION to "Facing direction",
        )
        SavedPropertyType.SHOP -> listOf(
            PropertyFieldKeys.SHOP_NAME to "Shop name",
            PropertyFieldKeys.LOCATION to "Location",
            PropertyFieldKeys.SIZE_CATEGORY to "Size category",
            PropertyFieldKeys.BUSINESS_TYPE to "Business type",
            PropertyFieldKeys.MAIN_ENTRANCE_FACING to "Main entrance facing",
            PropertyFieldKeys.COUNTER_LOCATION to "Counter location",
        )
        SavedPropertyType.FACTORY -> listOf(
            PropertyFieldKeys.FACILITY_NAME to "Facility name",
            PropertyFieldKeys.LOCATION to "Location",
            PropertyFieldKeys.FACILITY_TYPE to "Facility type",
            PropertyFieldKeys.TOTAL_AREA_SQ_FT to "Total area",
            PropertyFieldKeys.MACHINERY_ZONES to "Main machinery zones",
            PropertyFieldKeys.DISPATCH_AREA to "Dispatch / loading area",
            PropertyFieldKeys.SITE_ORIENTATION to "Site orientation",
        )
    }
    return required.filter { (key, _) -> details[key].orEmpty().isBlank() }.map { (_, label) -> label }
}

fun SavedProperty.saveBlockerMessage(uploadStates: Map<String, PropertyUploadState>): String? = when {
    uploadStates.hasBlockingUpload() -> "Wait for file uploads to finish before saving."
    hasPendingFileUploads() -> "A file is still being attached. Wait or remove it before saving."
    missingRequiredFieldLabels().isNotEmpty() ->
        "Required: ${missingRequiredFieldLabels().joinToString(", ")}"
    else -> null
}

fun SavedProperty.isValid(): Boolean = !hasPendingFileUploads() && when (type) {
    SavedPropertyType.HOME -> listOf(
        PropertyFieldKeys.PROJECT_NAME,
        PropertyFieldKeys.TOWER_NAME,
        PropertyFieldKeys.FLAT_NUMBER,
        PropertyFieldKeys.CONFIGURATION,
        PropertyFieldKeys.TOWER_FACING,
        PropertyFieldKeys.MOVE_IN_TIMELINE,
    ).all { details[it].orEmpty().isNotBlank() }
    SavedPropertyType.OFFICE -> listOf(
        PropertyFieldKeys.BUSINESS_NAME,
        PropertyFieldKeys.OFFICE_ADDRESS,
        PropertyFieldKeys.OFFICE_TYPE,
        PropertyFieldKeys.TOTAL_AREA_SQ_FT,
        PropertyFieldKeys.KEY_ZONES,
        PropertyFieldKeys.FACING_DIRECTION,
    ).all { details[it].orEmpty().isNotBlank() }
    SavedPropertyType.SHOP -> listOf(
        PropertyFieldKeys.SHOP_NAME,
        PropertyFieldKeys.LOCATION,
        PropertyFieldKeys.SIZE_CATEGORY,
        PropertyFieldKeys.BUSINESS_TYPE,
        PropertyFieldKeys.MAIN_ENTRANCE_FACING,
        PropertyFieldKeys.COUNTER_LOCATION,
    ).all { details[it].orEmpty().isNotBlank() }
    SavedPropertyType.FACTORY -> listOf(
        PropertyFieldKeys.FACILITY_NAME,
        PropertyFieldKeys.LOCATION,
        PropertyFieldKeys.FACILITY_TYPE,
        PropertyFieldKeys.TOTAL_AREA_SQ_FT,
        PropertyFieldKeys.MACHINERY_ZONES,
        PropertyFieldKeys.DISPATCH_AREA,
        PropertyFieldKeys.SITE_ORIENTATION,
    ).all { details[it].orEmpty().isNotBlank() }
}

fun SavedProperty.displaySubtitle(): String = when (type) {
    SavedPropertyType.HOME -> details[PropertyFieldKeys.EXACT_ADDRESS]
        ?: listOfNotNull(
            details[PropertyFieldKeys.PROJECT_NAME],
            details[PropertyFieldKeys.TOWER_NAME]?.let { "Tower $it" },
            details[PropertyFieldKeys.FLAT_NUMBER],
        ).joinToString(" · ")
    SavedPropertyType.OFFICE -> details[PropertyFieldKeys.OFFICE_ADDRESS].orEmpty()
    SavedPropertyType.SHOP -> details[PropertyFieldKeys.LOCATION].orEmpty()
    SavedPropertyType.FACTORY -> details[PropertyFieldKeys.LOCATION].orEmpty()
}

fun buildPropertyLabel(type: SavedPropertyType, details: Map<String, String>): String = when (type) {
    SavedPropertyType.HOME -> listOfNotNull(
        details[PropertyFieldKeys.PROJECT_NAME],
        details[PropertyFieldKeys.TOWER_NAME]?.let { "Tower $it" },
        details[PropertyFieldKeys.FLAT_NUMBER],
    ).joinToString(" · ")
    SavedPropertyType.OFFICE -> details[PropertyFieldKeys.BUSINESS_NAME].orEmpty()
    SavedPropertyType.SHOP -> details[PropertyFieldKeys.SHOP_NAME].orEmpty()
    SavedPropertyType.FACTORY -> details[PropertyFieldKeys.FACILITY_NAME].orEmpty()
}
