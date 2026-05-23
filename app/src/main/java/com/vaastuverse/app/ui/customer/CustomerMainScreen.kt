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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.vaastuverse.app.data.AppCoordinatorViewModel
import com.vaastuverse.app.data.AppUiState
import com.vaastuverse.app.data.CustomerOrder
import com.vaastuverse.app.data.OrderKind
import com.vaastuverse.app.data.PaymentCheckout
import com.vaastuverse.app.data.SavedProperty
import com.vaastuverse.app.data.SavedPropertyType
import com.vaastuverse.app.data.UserSessionViewModel
import com.vaastuverse.app.data.repository.OrderRepository
import com.vaastuverse.app.data.repository.PropertyRepository
import com.vaastuverse.app.ui.VvColors
import com.vaastuverse.app.ui.shell.AppMenuActions
import com.vaastuverse.app.ui.shell.CustomerNavController
import kotlinx.coroutines.launch

private enum class CustomerOverlay {
    None,
    UseCaseDetail,
    SampleReport,
    UserProfile,
    Properties,
    PropertyEditor,
    Payment,
}

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
    val scope = rememberCoroutineScope()
    val userId = state.session?.userId

    var tab by remember { mutableIntStateOf(0) }
    var overlay by remember { mutableStateOf(CustomerOverlay.None) }
    var selectedUseCaseId by remember { mutableStateOf<CustomerUseCaseId?>(null) }
    var properties by remember { mutableStateOf<List<SavedProperty>>(emptyList()) }
    var ongoingConsultations by remember { mutableStateOf<List<CustomerOrder>>(emptyList()) }
    var editingProperty by remember { mutableStateOf<SavedProperty?>(null) }
    var editorDefaultType by remember { mutableStateOf<SavedPropertyType?>(null) }
    var paymentCheckout by remember { mutableStateOf<PaymentCheckout?>(null) }

    val askLabel = "Ask ${session.leadGuideShortName}"
    val selectedUseCase = selectedUseCaseId?.let { CustomerUseCases.get(it) }

    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        launch {
            propertyRepo.propertiesFlow(id).collect { properties = it }
        }
    }

    LaunchedEffect(userId) {
        val id = userId ?: return@LaunchedEffect
        launch {
            orderRepo.openOrdersPageFlow(id).collect { ongoingConsultations = it }
        }
    }

    fun clearOverlayState() {
        overlay = CustomerOverlay.None
        selectedUseCaseId = null
        paymentCheckout = null
        editingProperty = null
        editorDefaultType = null
    }

    fun selectTab(index: Int) {
        tab = index
        clearOverlayState()
    }

    fun startCheckout(checkout: PaymentCheckout) {
        paymentCheckout = checkout
        overlay = CustomerOverlay.Payment
    }

    fun quickBuyUseCase(useCaseId: CustomerUseCaseId) {
        val useCase = CustomerUseCases.get(useCaseId)
        selectedUseCaseId = null
        startCheckout(
            PaymentCheckout(
                useCaseId = useCase.id,
                packageTitle = useCase.headerTitle,
                priceLabel = useCase.price,
                kind = OrderKind.REPORT,
            ),
        )
    }

    fun completePayment() {
        val checkout = paymentCheckout ?: return
        val id = userId ?: return
        scope.launch {
            orderRepo.addOrder(
                id,
                CustomerOrder(
                    useCaseId = checkout.useCaseId,
                    packageTitle = checkout.packageTitle,
                    priceLabel = checkout.priceLabel,
                    kind = checkout.kind,
                ),
            )
            paymentCheckout = null
            selectedUseCaseId = null
            overlay = CustomerOverlay.None
            tab = 0
        }
    }

    fun goBackFromPayment() {
        paymentCheckout = null
        overlay = if (selectedUseCaseId != null) CustomerOverlay.UseCaseDetail else CustomerOverlay.None
    }

    fun goBackFromSample() {
        overlay = CustomerOverlay.UseCaseDetail
    }

    fun goBackFromDetail() {
        overlay = CustomerOverlay.None
        selectedUseCaseId = null
    }

    fun goBackFromProfile() {
        overlay = CustomerOverlay.None
    }

    fun goBackFromProperties() {
        overlay = CustomerOverlay.None
    }

    fun goBackFromEditor() {
        overlay = CustomerOverlay.Properties
        editingProperty = null
        editorDefaultType = null
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
        CustomerOverlay.Properties -> CustomerTopBarMode.SubPage(
            title = "My properties",
            onBack = ::goBackFromProperties,
        )
        CustomerOverlay.PropertyEditor -> CustomerTopBarMode.SubPage(
            title = if (editingProperty == null) "Add property" else "Edit property",
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
                CustomerOverlay.SampleReport -> {
                    selectedUseCase?.let { useCase ->
                        CustomerMiddleScrollSection {
                            CustomerSampleReportScreen(
                                useCase = useCase,
                                onBookFactoryConsultation = if (useCase.id == CustomerUseCaseId.FACTORY) {
                                    {
                                        startCheckout(
                                            PaymentCheckout(
                                                useCaseId = CustomerUseCaseId.FACTORY,
                                                packageTitle = "Factory consultation",
                                                priceLabel = "₹1,999",
                                                kind = OrderKind.CONSULTATION,
                                            ),
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
                                onViewSampleReport = { overlay = CustomerOverlay.SampleReport },
                                onOrderReport = {
                                    startCheckout(
                                        PaymentCheckout(
                                            useCaseId = useCase.id,
                                            packageTitle = useCase.headerTitle,
                                            priceLabel = useCase.price,
                                            kind = OrderKind.REPORT,
                                        ),
                                    )
                                },
                            )
                        }
                    }
                }
                CustomerOverlay.UserProfile -> {
                    CustomerMiddleScrollSection {
                        CustomerProfileEditScreen(
                            profile = state.customerProfile,
                            isLoading = state.isLoading,
                            onSave = { name, city ->
                                coordinator.saveCustomerProfileInExperience(name, city)
                                overlay = CustomerOverlay.None
                            },
                        )
                    }
                }
                CustomerOverlay.Properties -> {
                    CustomerMiddleScrollSection {
                        CustomerPropertiesScreen(
                            properties = properties,
                            onAdd = {
                                editingProperty = null
                                editorDefaultType = null
                                overlay = CustomerOverlay.PropertyEditor
                            },
                            onEdit = { property ->
                                editingProperty = property
                                editorDefaultType = property.type
                                overlay = CustomerOverlay.PropertyEditor
                            },
                            onDelete = { property ->
                                val id = userId ?: return@CustomerPropertiesScreen
                                scope.launch {
                                    propertyRepo.deleteProperty(id, property.id)
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
                            onSave = { property ->
                                val id = userId ?: return@CustomerPropertyEditorScreen
                                scope.launch {
                                    propertyRepo.saveProperty(id, property)
                                    overlay = CustomerOverlay.Properties
                                    editingProperty = null
                                    editorDefaultType = null
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
                            ongoingOrders = ongoingConsultations,
                            onUseCaseClick = { id ->
                                selectedUseCaseId = id
                                overlay = CustomerOverlay.UseCaseDetail
                            },
                            onQuickBuy = ::quickBuyUseCase,
                        )
                    }
                    1 -> CustomerReportsScreen(
                        modifier = Modifier.fillMaxSize(),
                        userId = userId,
                        orderRepo = orderRepo,
                    )
                    2 -> CustomerMiddleScrollSection {
                        SimplePlaceholderScreen("Muhurats")
                    }
                    else -> CustomerMiddleScrollSection {
                        SimplePlaceholderScreen(askLabel)
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
