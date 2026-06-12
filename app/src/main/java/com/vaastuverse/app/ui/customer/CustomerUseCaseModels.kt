package com.vaastuverse.app.ui.customer

import com.vaastuverse.app.data.FeatureFlags

enum class CustomerUseCaseId {
    HOME,
    OFFICE,
    SHOP,
    FACTORY,
}

data class AnalysisPoint(
    val icon: String,
    val title: String,
    val detail: String,
)

data class CustomerUseCaseDetail(
    val id: CustomerUseCaseId,
    val icon: String,
    val headerTitle: String,
    val headerSubtitle: String,
    val badge: String,
    val intro: String,
    val analysisHeading: String,
    val analysisPoints: List<AnalysisPoint>,
    val priceLabel: String,
    val price: String,
    val marketCompare: String,
    val includes: String,
    val ctaLabel: String,
    val templateTitle: String,
    val templateDescription: String,
)

object CustomerUseCases {
    fun get(id: CustomerUseCaseId): CustomerUseCaseDetail = when (id) {
        CustomerUseCaseId.HOME -> home
        CustomerUseCaseId.OFFICE -> office
        CustomerUseCaseId.SHOP -> shop
        CustomerUseCaseId.FACTORY -> factory
    }

    private val home = CustomerUseCaseDetail(
        id = CustomerUseCaseId.HOME,
        icon = "🏠",
        headerTitle = "Home Vaastu Report",
        headerSubtitle = "Flats · Villas · Independent homes",
        badge = "Named Guruji validates every residential report",
        intro = "Home Vaastu focuses on health, harmony, and prosperity for your family — bedroom zones, kitchen placement, entrance energy, and remedies you can act on before possession or renovation.",
        analysisHeading = "WHAT WE ANALYSE FOR YOUR HOME",
        analysisPoints = listOf(
            AnalysisPoint("🚪", "Main entrance & foyer", "First impression energy · shoe rack · door alignment"),
            AnalysisPoint("🛏", "Master bedroom", "Sleep quality · SW or SW-adjacent zones · head direction"),
            AnalysisPoint("🍳", "Kitchen & stove", "Fire element · SE/ NW placement · sink vs stove"),
            AnalysisPoint("🛋", "Living & family zone", "Social harmony · TV placement · natural light"),
            AnalysisPoint("🕉", "Pooja / meditation", "NE placement · morning ritual · sacred zone"),
        ),
        priceLabel = "HOME VAASTU REPORT · GURUJI-VALIDATED",
        price = "₹399",
        marketCompare = "Consultant visit: ₹5,000–8,000",
        includes = "PDF report\nRemedy plan\nMuhurat tips",
        ctaLabel = "Get My Home Vaastu Report →",
        templateTitle = "Sample home report",
        templateDescription = "See a full example report for a Bengaluru apartment — zone scores, remedies, and Guruji’s sign-off.",
    )

    private val office = CustomerUseCaseDetail(
        id = CustomerUseCaseId.OFFICE,
        icon = "🏢",
        headerTitle = "Business Vaastu Report",
        headerSubtitle = "Office · Workspace",
        badge = "Only platform with business Vaastu + named Guruji expert",
        intro = "Business Vaastu is not the same as home Vaastu. Different zones matter for cash flow, team energy, client attraction, and owner cabin placement — validated by an accountable Guruji.",
        analysisHeading = "WHAT WE ANALYSE FOR YOUR OFFICE",
        analysisPoints = listOf(
            AnalysisPoint("🪑", "MD / Owner cabin", "Zone · facing direction · power position"),
            AnalysisPoint("💰", "Cash / finance area", "Accounts desk · safe placement · N or NE zone"),
            AnalysisPoint("🚪", "Main entrance energy", "Client attraction · reception placement · facing"),
            AnalysisPoint("👥", "Team / staff zone", "Workstation zones · productivity · collaboration"),
            AnalysisPoint("🕉", "Office mandir / pooja", "NE placement · morning ritual space"),
        ),
        priceLabel = "OFFICE VAASTU REPORT · GURUJI-VALIDATED",
        price = "₹599",
        marketCompare = "Consultant site visit: ₹8,000–15,000",
        includes = "PDF report\nRemedy plan\nAsk Guruji",
        ctaLabel = "Get My Office Vaastu Report →",
        templateTitle = "Sample office report",
        templateDescription = "Preview how we score MD cabin, finance corner, entrance, and team zones for a commercial workspace.",
    )

    private val shop = CustomerUseCaseDetail(
        id = CustomerUseCaseId.SHOP,
        icon = "🏪",
        headerTitle = "Shop & Showroom Report",
        headerSubtitle = "Retail · Showroom · Clinic frontage",
        badge = "Retail Vaastu tuned for footfall & cash flow",
        intro = "Shop Vaastu prioritises customer attraction at the entrance, cash counter placement, display energy, and owner visibility — the factors that directly affect daily sales.",
        analysisHeading = "WHAT WE ANALYSE FOR YOUR SHOP",
        analysisPoints = listOf(
            AnalysisPoint("🚪", "Shop entrance", "Footfall energy · signage · door orientation"),
            AnalysisPoint("💰", "Cash counter", "Billing zone · safe · customer payment flow"),
            AnalysisPoint("🛍", "Display & showroom", "Product zones · lighting · aisle flow"),
            AnalysisPoint("🪑", "Owner / manager desk", "Authority position · visibility of floor"),
            AnalysisPoint("📦", "Storage & back area", "Stock stability · clutter · rear energy"),
        ),
        priceLabel = "SHOP VAASTU REPORT · GURUJI-VALIDATED",
        price = "₹599",
        marketCompare = "Consultant visit: ₹10,000–18,000",
        includes = "PDF report\nRemedy plan\nAsk Guruji",
        ctaLabel = "Get My Shop Vaastu Report →",
        templateTitle = "Sample shop report",
        templateDescription = "See entrance, cash counter, and display-zone analysis for a retail showroom.",
    )

    private val factory = CustomerUseCaseDetail(
        id = CustomerUseCaseId.FACTORY,
        icon = "🏭",
        headerTitle = "Factory Vaastu Report",
        headerSubtitle = "Industrial · Warehouse · Plant",
        badge = "Industrial zones — not relabelled home Vaastu",
        intro = "Factory Vaastu covers production fire zones, raw material stability, dispatch movement, and owner authority — operational outcomes, not generic “prosperity” language.",
        analysisHeading = "WHAT WE ANALYSE FOR YOUR FACTORY",
        analysisPoints = listOf(
            AnalysisPoint("⚙", "Production zone (SE)", "Machinery placement · fire element · output energy"),
            AnalysisPoint("📦", "Raw material storage (SW)", "Earth stability · supply continuity"),
            AnalysisPoint("🚚", "Dispatch / finished goods (NW)", "Movement zone · staging · logistics flow"),
            AnalysisPoint("🪑", "MD / owner cabin (SW)", "Authority · stability · decision-making"),
            AnalysisPoint("👷", "Staff & utility zones", "Break areas · safety · workflow"),
        ),
        priceLabel = "FACTORY VAASTU REPORT · GURUJI-VALIDATED",
        price = "₹999",
        marketCompare = "Consultant site visit: ₹25,000+",
        includes = "PDF report\nRemedy plan\nConsultation upsell",
        ctaLabel = "Get My Factory Vaastu Report →",
        templateTitle = "Sample factory report",
        templateDescription = "Preview an industrial report with zone scores, dispatch remedy, and Guruji verdict.",
    )

    val landingCards = listOf(home, office, shop, factory)

    fun aiPackageTitle(useCase: CustomerUseCaseDetail): String =
        "${useCase.headerTitle} · AI"

    fun aiPriceLabel(): String = FeatureFlags.guruMatching.aiPriceLabel
}
