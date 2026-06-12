package com.vaastuverse.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.vaastuverse.app.data.ConsultationOffer
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.GuruTier
import com.vaastuverse.app.data.OrderKind
import com.vaastuverse.app.data.OrderStatus
import com.vaastuverse.app.data.PropertyBuyerInfo
import com.vaastuverse.app.data.PropertyValidationException
import com.vaastuverse.app.data.ReportFileHelper
import com.vaastuverse.app.data.canEditPropertyDetails
import com.vaastuverse.app.data.isPropertyLockedForEdit
import com.vaastuverse.app.data.isLocalOnly
import com.vaastuverse.app.data.isReportDelivered
import com.vaastuverse.app.data.isValid
import com.vaastuverse.app.data.needsPropertyDetails
import com.vaastuverse.app.data.PaymentCheckout
import com.vaastuverse.app.data.SavedProperty
import com.vaastuverse.app.data.SavedPropertyType
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.data.dto.hasDateOfBirth
import com.vaastuverse.app.data.repository.OrderRepository
import com.vaastuverse.app.data.repository.PropertyRepository
import com.vaastuverse.app.data.repository.PropertyUploadRepository
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.shared.CommunicationSettingsScreen
import com.vaastuverse.app.ui.shared.UserAccountProfileScreen
import com.vaastuverse.app.ui.shell.AppMenuActions
import com.vaastuverse.app.ui.shell.CustomerNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class CustomerOverlay {
    None,
    UseCaseDetail,
    SampleReport,
    UserProfile,
    Settings,
    Properties,
    OrderDetail,
    PropertySubmissionGate,
    PropertySelection,
    PropertyEditor,
    Payment,
}

private enum class PropertyEditorContext {
    ManageList,
    PostPaymentNew,
    PostPaymentEdit,
}

private data class CheckoutDraft(
    val useCaseId: CustomerUseCaseId,
    val packageTitle: String,
    val priceLabel: String,
    val kind: OrderKind,
    val orderId: String? = null,
    val buyerInfo: PropertyBuyerInfo = PropertyBuyerInfo(),
)

@Composable
fun CustomerMainScreen(
    modifier: Modifier = Modifier,
    state: AppUiState,
    coordinator: AppCoordinatorViewModel,
    session: UserSessionViewModel,
    customerNav: CustomerNavController,
    menuActions: AppMenuActions,
) {
    val context = LocalContext.current
    val propertyRepo = remember { PropertyRepository(context.applicationContext) }
    val orderRepo = remember { OrderRepository(context.applicationContext) }
    val uploadRepo = remember { PropertyUploadRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val sessionData = state.session
    val userId = sessionData?.userId

    var tab by remember { mutableIntStateOf(0) }
    var overlay by remember { mutableStateOf(CustomerOverlay.None) }
    var selectedUseCaseId by remember { mutableStateOf<CustomerUseCaseId?>(null) }
    var properties by remember { mutableStateOf<List<SavedProperty>>(emptyList()) }
    var allOpenOrders by remember { mutableStateOf<List<CustomerOrder>>(emptyList()) }
    val ongoingReports = allOpenOrders.filter { it.kind == OrderKind.REPORT }
    val ongoingConsultations = allOpenOrders.filter { it.kind == OrderKind.CONSULTATION }
    var editingProperty by remember { mutableStateOf<SavedProperty?>(null) }
    var editorDefaultType by remember { mutableStateOf<SavedPropertyType?>(null) }
    var editorContext by remember { mutableStateOf(PropertyEditorContext.ManageList) }
    var lockEditorType by remember { mutableStateOf(false) }
    var checkoutDraft by remember { mutableStateOf<CheckoutDraft?>(null) }
    var paymentCheckout by remember { mutableStateOf<PaymentCheckout?>(null) }
    var paymentReturnTab by remember { mutableIntStateOf(0) }
    var detailsFlowOriginTab by remember { mutableIntStateOf(0) }
    var selectedOrderForDetail by remember { mutableStateOf<CustomerOrder?>(null) }
    var orderClock by remember { mutableStateOf(System.currentTimeMillis()) }
    var aiGuruEnabled by remember { mutableStateOf(false) }
    var upgradePlanEnabled by remember { mutableStateOf(false) }
    var guruRatingForSelectedOrder by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedOrderForDetail?.id) {
        val order = selectedOrderForDetail
        val currentSession = sessionData
        guruRatingForSelectedOrder = if (order != null && currentSession != null && order.isReportDelivered()) {
            orderRepo.getGuruRating(currentSession, order.id)
        } else {
            null
        }
    }

    suspend fun refreshGuruMatchingFlags() {
        orderRepo.refreshFeatureFlags()
        aiGuruEnabled = com.vaastuverse.app.data.FeatureFlags.aiGuruEnabled
        upgradePlanEnabled = com.vaastuverse.app.data.FeatureFlags.upgradePlanEnabled
    }

    val askLabel = "Ask ${session.leadGuideShortName}"
    val selectedUseCase = selectedUseCaseId?.let { CustomerUseCases.get(it) }

    LaunchedEffect(userId, sessionData?.accessToken) {
        val id = userId ?: return@LaunchedEffect
        val session = sessionData ?: return@LaunchedEffect
        launch {
            runCatching {
                propertyRepo.propertiesFlow(id).collect { properties = it }
            }
        }
        launch {
            runCatching { propertyRepo.syncFromServer(session) }
        }
        launch {
            runCatching {
                refreshGuruMatchingFlags()
                orderRepo.syncFromServer(session)
            }
        }
    }

    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        launch {
            orderRepo.openOrdersPageFlow(id).collect { allOpenOrders = it }
        }
    }

    LaunchedEffect(userId, sessionData?.accessToken, orderClock) {
        val session = sessionData ?: return@LaunchedEffect
        runCatching { orderRepo.syncFromServer(session) }
    }

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect
        while (true) {
            delay(30_000)
            orderClock = System.currentTimeMillis()
        }
    }

    LaunchedEffect(allOpenOrders, selectedOrderForDetail?.id) {
        val orderId = selectedOrderForDetail?.id ?: return@LaunchedEffect
        allOpenOrders.find { it.id == orderId }?.let { selectedOrderForDetail = it }
    }

    fun clearOverlayState() {
        overlay = CustomerOverlay.None
        selectedUseCaseId = null
        selectedOrderForDetail = null
        paymentCheckout = null
        checkoutDraft = null
        editingProperty = null
        editorDefaultType = null
        editorContext = PropertyEditorContext.ManageList
        lockEditorType = false
    }

    fun refreshSelectedOrder() {
        val session = sessionData ?: return
        val orderId = selectedOrderForDetail?.id ?: return
        scope.launch {
            runCatching { orderRepo.syncFromServer(session) }
            val updated = orderRepo.loadSorted(session.userId).find { it.id == orderId }
            if (updated != null) selectedOrderForDetail = updated
        }
    }

    fun openOrderDetail(order: CustomerOrder) {
        detailsFlowOriginTab = tab
        selectedOrderForDetail = order
        overlay = CustomerOverlay.OrderDetail
    }

    fun selectTab(index: Int) {
        tab = index
        clearOverlayState()
    }

    fun beginOrderFlow(
        useCaseId: CustomerUseCaseId,
        packageTitle: String,
        priceLabel: String,
        kind: OrderKind = OrderKind.REPORT,
        guruTier: GuruTier = GuruTier.GURUJI_VALIDATED,
    ) {
        paymentReturnTab = tab
        paymentCheckout = PaymentCheckout(
            useCaseId = useCaseId,
            packageTitle = packageTitle,
            priceLabel = priceLabel,
            kind = kind,
            guruTier = guruTier,
        )
        overlay = CustomerOverlay.Payment
    }

    fun beginConsultationPayment(reportOrder: CustomerOrder, offer: ConsultationOffer) {
        paymentReturnTab = tab
        paymentCheckout = PaymentCheckout(
            useCaseId = reportOrder.useCaseId,
            packageTitle = "${offer.durationMinutes}-min call · ${reportOrder.packageTitle}",
            priceLabel = offer.priceLabel,
            kind = OrderKind.CONSULTATION,
            guruTier = reportOrder.guruTier,
            linkedReportOrderId = reportOrder.id,
            linkedReportTitle = reportOrder.packageTitle,
            consultationOffer = offer,
        )
        overlay = CustomerOverlay.Payment
    }

    fun openPropertyDetailsUi() {
        val draft = checkoutDraft ?: return
        val session = sessionData
        if (session != null) {
            scope.launch { runCatching { propertyRepo.syncFromServer(session) } }
        }
        val propertyType = SavedPropertyType.fromUseCaseId(draft.useCaseId)
        val matching = properties.filter { it.type == propertyType }
        if (matching.isEmpty()) {
            editingProperty = null
            editorDefaultType = propertyType
            editorContext = PropertyEditorContext.PostPaymentNew
            lockEditorType = true
            overlay = CustomerOverlay.PropertyEditor
        } else {
            overlay = CustomerOverlay.PropertySelection
        }
    }

    fun proceedAfterSubmissionGate(buyerInfo: PropertyBuyerInfo) {
        checkoutDraft = checkoutDraft?.copy(buyerInfo = buyerInfo)
        when (editorContext) {
            PropertyEditorContext.PostPaymentEdit -> {
                if (editingProperty != null) {
                    overlay = CustomerOverlay.PropertyEditor
                } else {
                    openPropertyDetailsUi()
                }
            }
            else -> openPropertyDetailsUi()
        }
    }

    fun beginPropertyDetailsFlow() {
        overlay = CustomerOverlay.PropertySubmissionGate
    }

    fun resumePropertyDetailsForOrder(order: CustomerOrder) {
        if (!order.needsPropertyDetails()) return
        selectedOrderForDetail = order
        detailsFlowOriginTab = tab
        editorContext = PropertyEditorContext.PostPaymentNew
        checkoutDraft = CheckoutDraft(
            useCaseId = order.useCaseId,
            packageTitle = order.packageTitle,
            priceLabel = order.priceLabel,
            kind = order.kind,
            orderId = order.id,
        )
        beginPropertyDetailsFlow()
    }

    fun beginPropertyEditForOrder(order: CustomerOrder) {
        if (!order.canEditPropertyDetails(orderClock)) return
        selectedOrderForDetail = order
        detailsFlowOriginTab = tab
        editorContext = PropertyEditorContext.PostPaymentEdit
        checkoutDraft = CheckoutDraft(
            useCaseId = order.useCaseId,
            packageTitle = order.packageTitle,
            priceLabel = order.priceLabel,
            kind = order.kind,
            orderId = order.id,
        )
        val property = properties.find { it.id == order.propertyId }
        editingProperty = property
        editorDefaultType = property?.type ?: SavedPropertyType.fromUseCaseId(order.useCaseId)
        lockEditorType = true
        if (property != null) {
            overlay = CustomerOverlay.PropertyEditor
        } else {
            beginPropertyDetailsFlow()
        }
    }

    fun handleOrderAction(order: CustomerOrder) {
        openOrderDetail(order)
    }

    fun openPropertyEditorToComplete(property: SavedProperty) {
        editingProperty = property
        editorDefaultType = property.type
        editorContext = PropertyEditorContext.PostPaymentNew
        lockEditorType = true
        overlay = CustomerOverlay.PropertyEditor
    }

    fun attachPropertyToOrder(property: SavedProperty) {
        val draft = checkoutDraft
        val session = sessionData
        val orderId = draft?.orderId
        if (draft == null || session == null || orderId == null) {
            coordinator.showUserMessage(
                "Open the report from Reports and add property details from there.",
                isError = true,
            )
            return
        }
        scope.launch {
            try {
                val fresh = if (property.isLocalOnly()) {
                    property
                } else {
                    propertyRepo.fetchProperty(session, property.id) ?: property
                }
                if (!fresh.isValid()) {
                    coordinator.showUserMessage(
                        "This property is missing required fields. Complete them to continue.",
                        isError = true,
                    )
                    openPropertyEditorToComplete(fresh)
                    return@launch
                }
                val propertyId = if (fresh.isLocalOnly()) {
                    propertyRepo.saveProperty(session, fresh).id
                } else {
                    fresh.id
                }
                val synced = orderRepo.linkPropertyToOrder(
                    session,
                    orderId,
                    propertyId,
                    draft.buyerInfo.buyerDifferentFromUser,
                    draft.buyerInfo.buyerFullName,
                    draft.buyerInfo.buyerDateOfBirthIso,
                )
                selectedOrderForDetail = synced
            } catch (e: PropertyValidationException) {
                coordinator.showUserMessage(e.message ?: "Invalid property details", isError = true)
                if (!property.isLocalOnly()) {
                    openPropertyEditorToComplete(property)
                }
                return@launch
            } catch (e: Exception) {
                coordinator.showUserMessage(e.message ?: "Could not submit property", isError = true)
                return@launch
            }
            editingProperty = null
            editorDefaultType = null
            lockEditorType = false
            editorContext = PropertyEditorContext.ManageList
            checkoutDraft = null
            overlay = CustomerOverlay.OrderDetail
        }
    }

    fun quickBuyUseCase(useCaseId: CustomerUseCaseId) {
        val useCase = CustomerUseCases.get(useCaseId)
        selectedUseCaseId = null
        beginOrderFlow(
            useCaseId = useCase.id,
            packageTitle = useCase.headerTitle,
            priceLabel = useCase.price,
            kind = OrderKind.REPORT,
        )
    }

    fun completePayment() {
        val checkout = paymentCheckout ?: return
        val session = sessionData ?: return
        scope.launch {
            try {
                val linkedReport = checkout.linkedReportOrderId?.let { reportId ->
                    orderRepo.loadSorted(session.userId).find { it.id == reportId }
                }
                val isConsultation = checkout.kind == OrderKind.CONSULTATION
                val savedOrder = orderRepo.addOrder(
                    session,
                    CustomerOrder(
                        useCaseId = checkout.useCaseId,
                        packageTitle = checkout.packageTitle,
                        priceLabel = checkout.priceLabel,
                        kind = checkout.kind,
                        guruTier = checkout.guruTier,
                        linkedReportOrderId = checkout.linkedReportOrderId,
                        propertyId = linkedReport?.propertyId,
                        propertyLabel = linkedReport?.propertyLabel,
                        assignedGurujiId = linkedReport?.assignedGurujiId,
                        assignedGurujiName = linkedReport?.assignedGurujiName,
                        status = if (isConsultation) OrderStatus.ONGOING else OrderStatus.AWAITING_DETAILS,
                    ),
                )
                paymentCheckout = null
                selectedUseCaseId = null
                if (isConsultation) {
                    checkoutDraft = null
                    overlay = CustomerOverlay.None
                    tab = 3
                    coordinator.showUserMessage(
                        "${checkout.consultationOffer?.label ?: "Consultation"} booked (${checkout.priceLabel})",
                    )
                } else {
                    detailsFlowOriginTab = paymentReturnTab.coerceAtLeast(0)
                    selectedOrderForDetail = savedOrder
                    checkoutDraft = CheckoutDraft(
                        useCaseId = savedOrder.useCaseId,
                        packageTitle = savedOrder.packageTitle,
                        priceLabel = savedOrder.priceLabel,
                        kind = savedOrder.kind,
                        orderId = savedOrder.id,
                    )
                    beginPropertyDetailsFlow()
                }
            } catch (e: Exception) {
                coordinator.showUserMessage(
                    e.message ?: "Payment succeeded but the order could not be created",
                    isError = true,
                )
            }
        }
    }

    fun goBackFromPayment() {
        paymentCheckout = null
        checkoutDraft = null
        overlay = CustomerOverlay.None
    }

    fun goBackFromPropertySubmissionGate() {
        if (selectedOrderForDetail != null) {
            checkoutDraft = null
            overlay = CustomerOverlay.OrderDetail
        } else {
            checkoutDraft = null
            overlay = CustomerOverlay.None
            tab = detailsFlowOriginTab
        }
    }

    fun goBackFromSample() {
        overlay = CustomerOverlay.UseCaseDetail
    }

    fun goBackFromDetail() {
        overlay = CustomerOverlay.None
        selectedUseCaseId = null
        checkoutDraft = null
    }

    fun goBackFromProfile() {
        overlay = CustomerOverlay.None
    }

    fun goBackFromSettings() {
        overlay = CustomerOverlay.None
    }

    fun goBackFromProperties() {
        overlay = CustomerOverlay.None
    }

    fun goBackFromPropertySelection() {
        checkoutDraft = null
        overlay = if (selectedOrderForDetail != null) {
            CustomerOverlay.OrderDetail
        } else {
            tab = detailsFlowOriginTab
            CustomerOverlay.None
        }
    }

    fun goBackFromOrderDetail() {
        selectedOrderForDetail = null
        overlay = CustomerOverlay.None
        tab = detailsFlowOriginTab
    }

    fun goBackFromEditor() {
        editingProperty = null
        editorDefaultType = null
        lockEditorType = false
        val context = editorContext
        overlay = when (context) {
            PropertyEditorContext.ManageList -> CustomerOverlay.Properties
            PropertyEditorContext.PostPaymentNew -> {
                val draft = checkoutDraft
                if (draft != null) {
                    val matching = properties.filter {
                        it.type == SavedPropertyType.fromUseCaseId(draft.useCaseId)
                    }
                    if (matching.isNotEmpty()) {
                        CustomerOverlay.PropertySelection
                    } else {
                        CustomerOverlay.PropertySubmissionGate
                    }
                } else if (selectedOrderForDetail != null) {
                    refreshSelectedOrder()
                    CustomerOverlay.OrderDetail
                } else {
                    tab = detailsFlowOriginTab
                    CustomerOverlay.None
                }
            }
            PropertyEditorContext.PostPaymentEdit -> {
                checkoutDraft = null
                if (selectedOrderForDetail != null) {
                    refreshSelectedOrder()
                    CustomerOverlay.OrderDetail
                } else {
                    tab = detailsFlowOriginTab
                    CustomerOverlay.None
                }
            }
        }
        editorContext = PropertyEditorContext.ManageList
    }

    val tabItems = listOf(
        CustomerTabItem("Home", Icons.Default.Home),
        CustomerTabItem("Reports", Icons.Default.Description),
        CustomerTabItem("Muhurats", Icons.Default.CalendarMonth),
        CustomerTabItem(askLabel, Icons.Default.Phone),
    )

    val topBarMode: CustomerTopBarMode = when (overlay) {
        CustomerOverlay.UseCaseDetail -> CustomerTopBarMode.SubPage(
            title = selectedUseCase?.headerTitle ?: "",
            onBack = ::goBackFromDetail,
        )
        CustomerOverlay.SampleReport -> CustomerTopBarMode.SubPage(
            title = "Sample report",
            onBack = ::goBackFromSample,
        )
        CustomerOverlay.UserProfile -> CustomerTopBarMode.SubPage(
            title = "My profile",
            onBack = ::goBackFromProfile,
        )
        CustomerOverlay.Settings -> CustomerTopBarMode.SubPage(
            title = "Settings",
            onBack = ::goBackFromSettings,
        )
        CustomerOverlay.Properties -> CustomerTopBarMode.SubPage(
            title = "My properties",
            onBack = ::goBackFromProperties,
        )
        CustomerOverlay.OrderDetail -> CustomerTopBarMode.SubPage(
            title = selectedOrderForDetail?.packageTitle ?: "Order",
            onBack = ::goBackFromOrderDetail,
        )
        CustomerOverlay.PropertySubmissionGate -> CustomerTopBarMode.SubPage(
            title = "Buyer details",
            onBack = ::goBackFromPropertySubmissionGate,
        )
        CustomerOverlay.PropertySelection -> CustomerTopBarMode.SubPage(
            title = "Property details",
            onBack = ::goBackFromPropertySelection,
        )
        CustomerOverlay.PropertyEditor -> CustomerTopBarMode.SubPage(
            title = when (editorContext) {
                PropertyEditorContext.PostPaymentEdit -> "Edit property for report"
                else -> if (editingProperty == null) "Add property" else "Edit property"
            },
            onBack = ::goBackFromEditor,
        )
        CustomerOverlay.Payment -> CustomerTopBarMode.SubPage(
            title = "Payment",
            onBack = ::goBackFromPayment,
        )
        CustomerOverlay.None -> when (tab) {
            0 -> CustomerTopBarMode.HomeWelcome
            else -> CustomerTopBarMode.TabTitle(tabItems[tab].label)
        }
    }

    LaunchedEffect(customerNav.openProfileRequest) {
        if (customerNav.openProfileRequest > 0) {
            overlay = CustomerOverlay.UserProfile
        }
    }

    LaunchedEffect(customerNav.openSettingsRequest) {
        if (customerNav.openSettingsRequest > 0) {
            overlay = CustomerOverlay.Settings
        }
    }

    LaunchedEffect(customerNav.openPropertiesRequest) {
        if (customerNav.openPropertiesRequest > 0) {
            overlay = CustomerOverlay.Properties
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VvColors.Cream),
    ) {
        CustomerTopBar(
            session = session,
            menuActions = menuActions,
            mode = topBarMode,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (overlay) {
                CustomerOverlay.Payment -> {
                    paymentCheckout?.let { checkout ->
                        CustomerMiddleScrollSection {
                            DummyPaymentScreen(
                                checkout = checkout,
                                onPaymentSuccess = ::completePayment,
                            )
                        }
                    }
                }
                CustomerOverlay.PropertySubmissionGate -> {
                    CustomerMiddleScrollSection {
                        PropertySubmissionGateScreen(
                            profileHasDob = state.customerProfile?.hasDateOfBirth() == true,
                            profileDateOfBirthIso = state.customerProfile?.dateOfBirth,
                            isLoading = state.isLoading,
                            onContinue = { result ->
                                val needsProfileDob = state.customerProfile?.hasDateOfBirth() != true
                                if (needsProfileDob && result.userDateOfBirthIso != null) {
                                    coordinator.saveCustomerDateOfBirth(result.userDateOfBirthIso) {
                                        proceedAfterSubmissionGate(result.buyerInfo)
                                    }
                                } else {
                                    proceedAfterSubmissionGate(result.buyerInfo)
                                }
                            },
                        )
                    }
                }
                CustomerOverlay.SampleReport -> {
                    selectedUseCase?.let { useCase ->
                        CustomerMiddleScrollSection {
                            CustomerSampleReportScreen(
                                useCase = useCase,
                                onBookFactoryConsultation = if (useCase.id == CustomerUseCaseId.FACTORY) {
                                    {
                                        beginOrderFlow(
                                            useCaseId = CustomerUseCaseId.FACTORY,
                                            packageTitle = "Factory consultation",
                                            priceLabel = "₹1,999",
                                            kind = OrderKind.CONSULTATION,
                                        )
                                    }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
                CustomerOverlay.UseCaseDetail -> {
                    selectedUseCase?.let { useCase ->
                        CustomerMiddleScrollSection {
                            CustomerUseCaseDetailScreen(
                                useCase = useCase,
                                session = session,
                                aiGuruEnabled = aiGuruEnabled,
                                onViewSampleReport = { overlay = CustomerOverlay.SampleReport },
                                onOrderReport = {
                                    beginOrderFlow(
                                        useCaseId = useCase.id,
                                        packageTitle = useCase.headerTitle,
                                        priceLabel = useCase.price,
                                        kind = OrderKind.REPORT,
                                    )
                                },
                                onOrderAiReport = {
                                    beginOrderFlow(
                                        useCaseId = useCase.id,
                                        packageTitle = CustomerUseCases.aiPackageTitle(useCase),
                                        priceLabel = CustomerUseCases.aiPriceLabel(),
                                        kind = OrderKind.REPORT,
                                        guruTier = GuruTier.AI,
                                    )
                                },
                            )
                        }
                    }
                }
                CustomerOverlay.PropertySelection -> {
                    checkoutDraft?.let { draft ->
                        val propertyType = SavedPropertyType.fromUseCaseId(draft.useCaseId)
                        val matching = properties.filter { it.type == propertyType }
                        CustomerMiddleScrollSection {
                            CustomerPropertySelectionScreen(
                                useCaseTitle = draft.packageTitle,
                                propertyType = propertyType,
                                properties = matching,
                                onSelectProperty = ::attachPropertyToOrder,
                                onAddNew = {
                                    editingProperty = null
                                    editorDefaultType = propertyType
                                    editorContext = PropertyEditorContext.PostPaymentNew
                                    lockEditorType = true
                                    overlay = CustomerOverlay.PropertyEditor
                                },
                            )
                        }
                    }
                }
                CustomerOverlay.UserProfile -> {
                    CustomerMiddleScrollSection {
                        UserAccountProfileScreen(
                            account = state.account,
                            profile = state.customerProfile,
                            isLoading = state.isLoading,
                            onSave = { name, city, dob ->
                                coordinator.saveCustomerProfileInExperience(name, city, dob)
                                coordinator.refreshAccount()
                                overlay = CustomerOverlay.None
                            },
                        )
                    }
                }
                CustomerOverlay.Settings -> {
                    CustomerMiddleScrollSection {
                        CommunicationSettingsScreen(
                            preferences = state.communicationPreferences,
                            onChange = coordinator::updateCommunicationPreferences,
                        )
                    }
                }
                CustomerOverlay.OrderDetail -> {
                    selectedOrderForDetail?.let { order ->
                        val linkedProperty = order.propertyId?.let { id ->
                            properties.find { it.id == id }
                        }
                        CustomerMiddleScrollSection {
                            CustomerOrderDetailScreen(
                                order = order,
                                property = linkedProperty,
                                nowMs = orderClock,
                                upgradePlanEnabled = upgradePlanEnabled,
                                guruRating = guruRatingForSelectedOrder,
                                onSubmitGuruRating = { rating ->
                                    scope.launch {
                                        val currentSession = sessionData ?: return@launch
                                        val ok = orderRepo.submitGuruRating(currentSession, order.id, rating)
                                        if (ok) {
                                            guruRatingForSelectedOrder = rating
                                            Toast.makeText(
                                                context,
                                                "Thanks for rating your Guruji!",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Could not submit rating. Try again.",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                                },
                                onAddPropertyDetails = { resumePropertyDetailsForOrder(order) },
                                onEditProperty = { beginPropertyEditForOrder(order) },
                                onPreviewReport = {
                                    scope.launch {
                                        val currentSession = sessionData ?: return@launch
                                        val previewUrl = orderRepo.getReportAccessUrl(
                                            currentSession,
                                            order.id,
                                            "preview",
                                        )
                                        if (previewUrl.isNullOrBlank()) {
                                            Toast.makeText(
                                                context,
                                                "Could not open preview. Try again.",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                            return@launch
                                        }
                                        runCatching {
                                            ReportFileHelper.openPreviewUrl(context, previewUrl)
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "No PDF viewer found on this device.",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                        }
                                    }
                                },
                                onDownloadReport = {
                                    scope.launch {
                                        val currentSession = sessionData ?: return@launch
                                        val cached = ReportFileHelper.findCachedReport(context, order.id)
                                        if (cached != null) {
                                            runCatching {
                                                ReportFileHelper.openLocalPdf(context, cached)
                                            }.onFailure {
                                                Toast.makeText(
                                                    context,
                                                    "Could not open saved report.",
                                                    Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                            return@launch
                                        }
                                        val uri = orderRepo.downloadReportToDevice(currentSession, order.id)
                                        if (uri == null) {
                                            Toast.makeText(
                                                context,
                                                "Download failed. Try again.",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                            return@launch
                                        }
                                        runCatching {
                                            ReportFileHelper.openLocalPdf(context, uri)
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Report saved to Downloads.",
                                                Toast.LENGTH_LONG,
                                            ).show()
                                        }
                                    }
                                },
                                onUpgradeToGuruji = {
                                    scope.launch {
                                        val currentSession = sessionData ?: return@launch
                                        val upgraded = orderRepo.upgradeToGurujiValidation(currentSession, order.id)
                                        if (upgraded == null) {
                                            Toast.makeText(
                                                context,
                                                "Upgrade unavailable. Try again later.",
                                                Toast.LENGTH_SHORT,
                                            ).show()
                                            return@launch
                                        }
                                        selectedOrderForDetail = upgraded
                                        Toast.makeText(
                                            context,
                                            "Upgrade started — Guruji will review your report.",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                                },
                                onBookConsultation = { offer ->
                                    beginConsultationPayment(order, offer)
                                },
                            )
                        }
                    }
                }
                CustomerOverlay.Properties -> {
                    CustomerMiddleScrollSection {
                        CustomerPropertiesScreen(
                            properties = properties,
                            isPropertyEditLocked = { property ->
                                isPropertyLockedForEdit(property.id, allOpenOrders, orderClock)
                            },
                            onAdd = {
                                editingProperty = null
                                editorDefaultType = null
                                editorContext = PropertyEditorContext.ManageList
                                lockEditorType = false
                                overlay = CustomerOverlay.PropertyEditor
                            },
                            onEdit = { property ->
                                editingProperty = property
                                editorDefaultType = property.type
                                editorContext = PropertyEditorContext.ManageList
                                lockEditorType = false
                                overlay = CustomerOverlay.PropertyEditor
                            },
                            onDelete = { property ->
                                val currentSession = sessionData ?: return@CustomerPropertiesScreen
                                scope.launch {
                                    propertyRepo.deleteProperty(currentSession, property.id)
                                }
                            },
                        )
                    }
                }
                CustomerOverlay.PropertyEditor -> {
                    CustomerMiddleScrollSection {
                        CustomerPropertyEditorScreen(
                            existing = editingProperty,
                            defaultType = editorDefaultType,
                            lockType = lockEditorType,
                            session = sessionData,
                            uploadRepo = uploadRepo,
                            onNotify = { message -> coordinator.showUserMessage(message) },
                            onSave = { property ->
                                val currentSession = sessionData ?: run {
                                    coordinator.showUserMessage("Sign in to save properties", isError = true)
                                    return@CustomerPropertyEditorScreen
                                }
                                scope.launch {
                                    try {
                                        val saved = propertyRepo.saveProperty(currentSession, property)
                                        when (editorContext) {
                                            PropertyEditorContext.ManageList -> {
                                                coordinator.showUserMessage("Property saved")
                                                overlay = CustomerOverlay.Properties
                                                editingProperty = null
                                                editorDefaultType = null
                                            }
                                            PropertyEditorContext.PostPaymentNew,
                                            PropertyEditorContext.PostPaymentEdit,
                                            -> attachPropertyToOrder(saved)
                                        }
                                    } catch (e: PropertyValidationException) {
                                        coordinator.showUserMessage(
                                            e.message ?: "Invalid property details",
                                            isError = true,
                                        )
                                    } catch (e: Exception) {
                                        coordinator.showUserMessage(
                                            e.message ?: "Could not save property",
                                            isError = true,
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
                CustomerOverlay.None -> when (tab) {
                    0 -> CustomerMiddleScrollSection(
                        background = CustomerMiddleThemes.homeGradient,
                    ) {
                        CustomerHomeScreen(
                            session = session,
                            ongoingReports = ongoingReports,
                            ongoingConsultations = ongoingConsultations,
                            onUseCaseClick = { id ->
                                selectedUseCaseId = id
                                overlay = CustomerOverlay.UseCaseDetail
                            },
                            onQuickBuy = ::quickBuyUseCase,
                            orderNowMs = orderClock,
                            onOrderAction = { order -> handleOrderAction(order) },
                        )
                    }
                    1 -> CustomerReportsScreen(
                        modifier = Modifier.fillMaxSize(),
                        userId = userId,
                        orderRepo = orderRepo,
                        orderNowMs = orderClock,
                        onOrderAction = { order -> handleOrderAction(order) },
                    )
                    2 -> CustomerMiddleScrollSection {
                        SimplePlaceholderScreen("Muhurats")
                    }
                    else -> CustomerMiddleScrollSection {
                        CustomerAskGurujiScreen(
                            userId = userId,
                            leadGuideName = session.leadGuideShortName,
                            orderRepo = orderRepo,
                            onBuyConsultation = ::beginConsultationPayment,
                            onViewReport = { order -> openOrderDetail(order) },
                        )
                    }
                }
            }
        }

        CustomerBottomBar(
            tabs = tabItems,
            selectedIndex = tab,
            onTabSelected = ::selectTab,
        )
    }
}
