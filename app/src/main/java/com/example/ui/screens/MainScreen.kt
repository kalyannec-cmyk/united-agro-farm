package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.components.AttachedDocumentItem
import com.example.ui.components.AttachedDocumentsRow
import com.example.ui.components.DocumentUploadMenuDialog
import com.example.ui.components.MuktinathPaymentRequestCard
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.FarmOfficialInfo
import com.example.model.OrderStatus
import com.example.model.UserRole
import com.example.ui.components.CompanyLogo
import com.example.ui.components.OrderReceiptDialog
import com.example.ui.theme.AgroGreenPrimary
import com.example.ui.viewmodel.FarmScreen
import com.example.ui.viewmodel.FarmViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * MainScreen provides the core screen layout with a Material 3 NavigationBar
 * configured to switch between the 3 primary farm management sections:
 *  1. Inventory (मौज्दात)
 *  2. Orders (अर्डर तथा रसिद)
 *  3. Price List (मूल्य सूची)
 *
 * Additional modules (Farm Overview/Home, Staff Attendance, Finance, Bank Transfer QR,
 * Recharts Dashboard, and Official Documents) remain accessible through the Drawer
 * and Top App Bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentUserId: String,
    onSignOut: () -> Unit,
    viewModel: FarmViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isEnglish by viewModel.isEnglishMode.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val inventoryFilter by viewModel.inventoryFilter.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val allProducts by viewModel.products.collectAsStateWithLifecycle()
    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val inventoryFilteredProducts by viewModel.inventoryFilteredProducts.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val selectedOrderForReceipt by viewModel.selectedOrderForReceipt.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val dailyAttendance by viewModel.dailyAttendance.collectAsStateWithLifecycle()
    val monthlyAttendance by viewModel.monthlyAttendance.collectAsStateWithLifecycle()
    val selectedAttendanceDate by viewModel.selectedAttendanceDate.collectAsStateWithLifecycle()
    val selectedAttendanceMonth by viewModel.selectedAttendanceMonth.collectAsStateWithLifecycle()

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val balanceSheetSummary by viewModel.balanceSheetSummary.collectAsStateWithLifecycle()
    val balanceSheetLedger by viewModel.balanceSheetLedger.collectAsStateWithLifecycle()

    val monthlyDashboardSummary by viewModel.monthlyDashboardSummary.collectAsStateWithLifecycle()
    val selectedDashboardMonth by viewModel.selectedDashboardMonth.collectAsStateWithLifecycle()

    val filteredDocuments by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val documentSearchQuery by viewModel.documentSearchQuery.collectAsStateWithLifecycle()
    val documentCategoryFilter by viewModel.documentCategoryFilter.collectAsStateWithLifecycle()
    val documentStatusFilter by viewModel.documentStatusFilter.collectAsStateWithLifecycle()

    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val roomOrders by viewModel.roomOrders.collectAsStateWithLifecycle()
    val activePaymentOrder by viewModel.activePaymentOrder.collectAsStateWithLifecycle()
    val attachedDocuments by viewModel.attachedDocuments.collectAsStateWithLifecycle()
    var showUploadMenuDialog by remember { mutableStateOf(false) }

    var showProfileDialog by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Handle back button: return to Inventory (primary section) if currently on another screen
    BackHandler(enabled = currentScreen != FarmScreen.INVENTORY) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.navigateTo(FarmScreen.INVENTORY)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(320.dp)
                    .testTag("navigation_drawer")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Drawer Header with Farm Branding
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CompanyLogo(size = 52.dp, showSubtext = false)
                            Column {
                                Text(
                                    text = if (isEnglish) FarmOfficialInfo.FARM_NAME_EN else FarmOfficialInfo.FARM_NAME_NP,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AgroGreenPrimary
                                )
                                Text(
                                    text = if (isEnglish) FarmOfficialInfo.LOCATION_EN else FarmOfficialInfo.LOCATION_NP,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = if (isEnglish) "MAIN SECTIONS" else "मुख्य खण्डहरू",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )

                    // Navigation to the 3 main sections
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                        label = { Text(if (isEnglish) "Inventory (Stock)" else "इन्भेन्ट्रि (मौज्दात)") },
                        selected = currentScreen == FarmScreen.INVENTORY,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.INVENTORY)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_inventory")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                        label = { Text(if (isEnglish) "Orders & Receipts" else "अर्डर तथा रसिद") },
                        selected = currentScreen == FarmScreen.ORDERS,
                        badge = {
                            val pending = orders.count { it.status == OrderStatus.PENDING_VERIFICATION }
                            if (pending > 0) {
                                Badge(containerColor = Color(0xFFEF6C00)) { Text("$pending") }
                            }
                        },
                        onClick = {
                            viewModel.navigateTo(FarmScreen.ORDERS)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_orders")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.PriceChange, contentDescription = null) },
                        label = { Text(if (isEnglish) "Price List & Cart" else "मूल्य सूची तथा कार्ट") },
                        selected = currentScreen == FarmScreen.PRICE_LIST,
                        badge = {
                            if (cart.items.isNotEmpty()) {
                                Badge(containerColor = Color(0xFFE65100)) { Text("${cart.totalItemCount}") }
                            }
                        },
                        onClick = {
                            viewModel.navigateTo(FarmScreen.PRICE_LIST)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_price_list")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                        label = { Text(if (isEnglish) "Room Inventory Table (LazyColumn)" else "Room मौज्दात सूची (LazyColumn)") },
                        selected = currentScreen == FarmScreen.ROOM_INVENTORY,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.ROOM_INVENTORY)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_room_inventory")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                        label = { Text(if (isEnglish) "Create New Order Form" else "नयाँ अर्डर फारम") },
                        selected = currentScreen == FarmScreen.CREATE_ORDER,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.CREATE_ORDER)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_create_order")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Payment, contentDescription = null) },
                        label = { Text(if (isEnglish) "Muktinath Payment Request" else "मुक्तिनाथ बैंक भुक्तानी अनुरोध") },
                        selected = currentScreen == FarmScreen.MUKTINATH_PAYMENT,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.MUKTINATH_PAYMENT)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_muktinath_payment")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null) },
                        label = { Text(if (isEnglish) "Past Orders (Room DB)" else "विगतका अर्डरहरू (Room DB)") },
                        selected = currentScreen == FarmScreen.PAST_ORDERS,
                        badge = {
                            if (roomOrders.isNotEmpty()) {
                                Badge(containerColor = AgroGreenPrimary) { Text("${roomOrders.size}") }
                            }
                        },
                        onClick = {
                            viewModel.navigateTo(FarmScreen.PAST_ORDERS)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_past_orders")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                        label = { Text(if (isEnglish) "Monthly Revenue (D3/Recharts)" else "मासिक आम्दानी (D3/Recharts)") },
                        selected = currentScreen == FarmScreen.MONTHLY_REVENUE,
                        badge = {
                            Badge(containerColor = Color(0xFFF57F17)) { Text("D3") }
                        },
                        onClick = {
                            viewModel.navigateTo(FarmScreen.MONTHLY_REVENUE)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_monthly_revenue")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                        label = { Text(if (isEnglish) "Upload Images & Documents" else "फोटो तथा कागजात अपलोड") },
                        selected = false,
                        onClick = {
                            showUploadMenuDialog = true
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_upload_documents")
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(
                        text = if (isEnglish) "MANAGEMENT & FINANCE" else "व्यवस्थापन तथा वित्तीय",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text(if (isEnglish) "Farm Overview" else "फार्म गृह पृष्ठ") },
                        selected = currentScreen == FarmScreen.HOME,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.HOME)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_home")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                        label = { Text(if (isEnglish) "Staff Attendance & Wage" else "कर्मचारी हाजिरी तथा ज्याला") },
                        selected = currentScreen == FarmScreen.ATTENDANCE,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.ATTENDANCE)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_attendance")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.MonetizationOn, contentDescription = null) },
                        label = { Text(if (isEnglish) "Finance & Balance Sheet" else "आय-व्यय तथा ब्यालेन्स सिट") },
                        selected = currentScreen == FarmScreen.FINANCE,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.FINANCE)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_finance")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                        label = { Text(if (isEnglish) "Muktinath Bank QR & Transfer" else "मुक्तिनाथ बैंक भुक्तानी") },
                        selected = currentScreen == FarmScreen.BANK_TRANSFER,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.BANK_TRANSFER)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_bank")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                        label = { Text(if (isEnglish) "Recharts Business Analytics" else "ड्यासबोर्ड (Recharts)") },
                        selected = currentScreen == FarmScreen.DASHBOARD,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.DASHBOARD)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_dashboard")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                        label = { Text(if (isEnglish) "Official Documents & Permits" else "कागजात तथा इजाजतपत्र") },
                        selected = currentScreen == FarmScreen.DOCUMENTS,
                        onClick = {
                            viewModel.navigateTo(FarmScreen.DOCUMENTS)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_documents")
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    HorizontalDivider()

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                        label = { Text(if (isEnglish) "User Profile & Security" else "प्रयोगकर्ता प्रोफाइल") },
                        selected = false,
                        onClick = {
                            showProfileDialog = true
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).testTag("drawer_item_profile")
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("main_screen_scaffold"),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                }
                            },
                            modifier = Modifier.testTag("top_bar_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Farm Menu",
                                tint = Color.White
                            )
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.clickable { viewModel.navigateTo(FarmScreen.HOME) }
                        ) {
                            CompanyLogo(size = 32.dp, showSubtext = false)
                            Text(
                                text = when (currentScreen) {
                                    FarmScreen.INVENTORY -> if (isEnglish) "Inventory" else "मौज्दात सूची"
                                    FarmScreen.ROOM_INVENTORY -> if (isEnglish) "Room Inventory" else "Room मौज्दात"
                                    FarmScreen.CREATE_ORDER -> if (isEnglish) "Create Order" else "नयाँ अर्डर"
                                    FarmScreen.MUKTINATH_PAYMENT -> if (isEnglish) "Muktinath Payment" else "मुक्तिनाथ भुक्तानी"
                                    FarmScreen.PAST_ORDERS -> if (isEnglish) "Past Orders" else "विगतका अर्डरहरू"
                                    FarmScreen.MONTHLY_REVENUE -> if (isEnglish) "Monthly Revenue (D3/Recharts)" else "मासिक आम्दानी (D3/Recharts)"
                                    FarmScreen.ORDERS -> if (isEnglish) "Orders" else "अर्डर सूची"
                                    FarmScreen.PRICE_LIST -> if (isEnglish) "Price List" else "मूल्य सूची"
                                    FarmScreen.HOME -> if (isEnglish) FarmOfficialInfo.FARM_NAME_EN else FarmOfficialInfo.FARM_NAME_NP
                                    FarmScreen.DASHBOARD -> if (isEnglish) "Dashboard" else "ड्यासबोर्ड"
                                    FarmScreen.ATTENDANCE -> if (isEnglish) "Staff Attendance" else "कर्मचारी हाजिरी"
                                    FarmScreen.FINANCE -> if (isEnglish) "Finance Ledger" else "आय-व्यय हिसाब"
                                    FarmScreen.BANK_TRANSFER -> if (isEnglish) "Muktinath Bank" else "मुक्तिनाथ बैंक"
                                    FarmScreen.DOCUMENTS -> if (isEnglish) "Documents & Permits" else "कागजात तथा इजाजत"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AgroGreenPrimary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        // Upload Company Image / Attached Document
                        IconButton(
                            onClick = { showUploadMenuDialog = true },
                            modifier = Modifier.testTag("top_bar_upload_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Upload Company Image / Documents",
                                tint = Color.White
                            )
                        }

                        // Quick switch to Farm Overview
                        IconButton(
                            onClick = { viewModel.navigateTo(FarmScreen.HOME) },
                            modifier = Modifier.testTag("top_bar_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Farm Overview",
                                tint = if (currentScreen == FarmScreen.HOME) Color(0xFFFFD54F) else Color.White
                            )
                        }

                        // Language Toggle button
                        IconButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier.testTag("toggle_language_button")
                        ) {
                            Text(
                                text = if (isEnglish) "नेपाली" else "EN",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // User Profile Dialog
                        IconButton(
                            onClick = { showProfileDialog = true },
                            modifier = Modifier.testTag("top_bar_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Profile & Security",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = {
                // Material 3 NavigationBar configured specifically to switch between Inventory, Orders, and Price List
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    // 1. Inventory Section
                    NavigationBarItem(
                        selected = currentScreen == FarmScreen.INVENTORY,
                        onClick = { viewModel.navigateTo(FarmScreen.INVENTORY) },
                        icon = {
                            val lowStockCount = allProducts.count { it.isLowStock }
                            if (lowStockCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Color(0xFFD32F2F)) {
                                            Text("$lowStockCount")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = "Inventory")
                                }
                            } else {
                                Icon(Icons.Default.Inventory2, contentDescription = "Inventory")
                            }
                        },
                        label = {
                            Text(
                                text = if (isEnglish) "Inventory" else "मौज्दात",
                                fontWeight = if (currentScreen == FarmScreen.INVENTORY) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgroGreenPrimary,
                            indicatorColor = AgroGreenPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_inventory")
                    )

                    // 2. Orders Section
                    NavigationBarItem(
                        selected = currentScreen == FarmScreen.ORDERS,
                        onClick = { viewModel.navigateTo(FarmScreen.ORDERS) },
                        icon = {
                            val pending = orders.count { it.status == OrderStatus.PENDING_VERIFICATION }
                            if (pending > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Color(0xFFEF6C00)) {
                                            Text("$pending")
                                        }
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Orders")
                                }
                            } else {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Orders")
                            }
                        },
                        label = {
                            Text(
                                text = if (isEnglish) "Orders" else "अर्डर",
                                fontWeight = if (currentScreen == FarmScreen.ORDERS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgroGreenPrimary,
                            indicatorColor = AgroGreenPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_orders")
                    )

                    // 3. Price List Section
                    NavigationBarItem(
                        selected = currentScreen == FarmScreen.PRICE_LIST,
                        onClick = { viewModel.navigateTo(FarmScreen.PRICE_LIST) },
                        icon = {
                            if (cart.items.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = Color(0xFFE65100)) {
                                            Text("${cart.totalItemCount}")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.PriceChange, contentDescription = "Price List")
                                }
                            } else {
                                Icon(Icons.Default.PriceChange, contentDescription = "Price List")
                            }
                        },
                        label = {
                            Text(
                                text = if (isEnglish) "Price List" else "मूल्य सूची",
                                fontWeight = if (currentScreen == FarmScreen.PRICE_LIST) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgroGreenPrimary,
                            indicatorColor = AgroGreenPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_price_list")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    FarmScreen.INVENTORY -> {
                        InventoryScreen(
                            products = inventoryFilteredProducts,
                            allProducts = allProducts,
                            searchQuery = searchQuery,
                            filter = inventoryFilter,
                            isEnglish = isEnglish,
                            onSearchChange = { q -> viewModel.setSearchQuery(q) },
                            onFilterChange = { f -> viewModel.setInventoryFilter(f) },
                            onStockAdjust = { id, delta -> viewModel.adjustProductStock(id, delta) },
                            onStockSet = { id, stock -> viewModel.updateProductStock(id, stock) },
                            onAddProduct = { prod -> viewModel.addNewProduct(prod) },
                            onUpdateProductPriceAndStock = { id, rPrice, wPrice, stock ->
                                viewModel.updateProductPriceAndStock(id, rPrice, wPrice, stock)
                            },
                            onDeleteProduct = { id -> viewModel.deleteProduct(id) },
                            onNavigateToRoomInventory = { viewModel.navigateTo(FarmScreen.ROOM_INVENTORY) }
                        )
                    }

                    FarmScreen.ORDERS -> {
                        OrdersScreen(
                            orders = orders,
                            isEnglish = isEnglish,
                            onSelectOrder = { order -> viewModel.openReceipt(order) },
                            onVerifyOrder = { id -> viewModel.verifyOrderPayment(id) },
                            onNavigateToCreateOrder = { viewModel.navigateTo(FarmScreen.CREATE_ORDER) },
                            onNavigateToPastOrders = { viewModel.navigateTo(FarmScreen.PAST_ORDERS) }
                        )
                    }

                    FarmScreen.PRICE_LIST -> {
                        PriceListScreen(
                            products = filteredProducts,
                            cart = cart,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            sortOrder = sortOrder,
                            isEnglish = isEnglish,
                            onSearchChange = { q -> viewModel.setSearchQuery(q) },
                            onCategoryChange = { cat -> viewModel.setCategory(cat) },
                            onSortChange = { s -> viewModel.setSortOrder(s) },
                            onAddToCart = { prod -> viewModel.addToCart(prod) },
                            onUpdateCartQty = { id, qty -> viewModel.updateCartQuantity(id, qty) },
                            onClearCart = { viewModel.clearCart() },
                            onCheckoutSubmit = { name, phone, addr, bank, notes ->
                                viewModel.checkoutOrder(name, phone, addr, bank, notes)
                            },
                            onUpdateProductPriceAndStock = { id, rPrice, wPrice, stock ->
                                viewModel.updateProductPriceAndStock(id, rPrice, wPrice, stock)
                            }
                        )
                    }

                    FarmScreen.HOME -> {
                        HomeScreen(
                            products = allProducts,
                            isEnglish = isEnglish,
                            onNavigate = { screen -> viewModel.navigateTo(screen) },
                            onToast = { msg -> viewModel.showToast(msg) }
                        )
                    }

                    FarmScreen.DASHBOARD -> {
                        DashboardScreen(
                            summary = monthlyDashboardSummary,
                            selectedMonth = selectedDashboardMonth,
                            balanceSheet = balanceSheetSummary,
                            balanceSheetLedger = balanceSheetLedger,
                            transactions = transactions,
                            isEnglish = isEnglish,
                            onMonthChange = { m -> viewModel.setDashboardMonth(m) },
                            onNavigate = { screen -> viewModel.navigateTo(screen) },
                            onBack = { viewModel.navigateTo(FarmScreen.INVENTORY) }
                        )
                    }

                    FarmScreen.ATTENDANCE -> {
                        AttendanceScreen(
                            employees = employees,
                            dailyAttendance = dailyAttendance,
                            monthlyAttendance = monthlyAttendance,
                            selectedDate = selectedAttendanceDate,
                            selectedMonth = selectedAttendanceMonth,
                            isEnglish = isEnglish,
                            onDateChange = { d -> viewModel.setAttendanceDate(d) },
                            onMonthChange = { m -> viewModel.setAttendanceMonth(m) },
                            onMarkAttendance = { emp, status, inTime, remarks ->
                                viewModel.markEmployeeAttendance(emp, status, inTime, "05:00 PM", remarks)
                            },
                            onMarkAllPresent = { d -> viewModel.markAllPresent(d) },
                            onAddEmployee = { name, role, phone, wage, salary ->
                                viewModel.addNewEmployee(name, role, phone, wage, salary)
                            },
                            onUpdateEmployee = { emp -> viewModel.updateEmployee(emp) },
                            onDeleteEmployee = { id -> viewModel.deleteEmployee(id) }
                        )
                    }

                    FarmScreen.FINANCE -> {
                        FinanceScreen(
                            transactions = transactions,
                            balanceSheet = balanceSheetSummary,
                            balanceSheetLedger = balanceSheetLedger,
                            dashboardSummary = monthlyDashboardSummary,
                            selectedDate = selectedAttendanceDate,
                            isEnglish = isEnglish,
                            onDateChange = { d -> viewModel.setAttendanceDate(d) },
                            onAddTransaction = { type, cat, amt, mode, party, ref, desc, date ->
                                viewModel.recordDailyTransaction(type, cat, amt, date, mode, party, ref, desc)
                            },
                            onDeleteTransaction = { id -> viewModel.deleteTransaction(id) },
                            onUpdateLedger = { updated -> viewModel.updateBalanceSheetLedger(updated) },
                            onOpenDashboard = { viewModel.navigateTo(FarmScreen.DASHBOARD) }
                        )
                    }

                    FarmScreen.BANK_TRANSFER -> {
                        BankTransferScreen(
                            orders = orders,
                            isEnglish = isEnglish,
                            onToast = { msg -> viewModel.showToast(msg) },
                            onSubmitDirectTransfer = { name, phone, addr, bank, sender, ref, amt, rem ->
                                viewModel.submitDirectBankTransfer(name, phone, addr, bank, sender, ref, amt, rem)
                            },
                            onOpenReceipt = { order -> viewModel.openReceipt(order) }
                        )
                    }

                    FarmScreen.DOCUMENTS -> {
                        DocumentsScreen(
                            documents = filteredDocuments,
                            searchQuery = documentSearchQuery,
                            selectedCategory = documentCategoryFilter,
                            selectedStatus = documentStatusFilter,
                            isEnglish = isEnglish,
                            onSearchChange = { q -> viewModel.setDocumentSearch(q) },
                            onCategoryFilterChange = { cat -> viewModel.setDocumentCategoryFilter(cat) },
                            onStatusFilterChange = { st -> viewModel.setDocumentStatusFilter(st) },
                            onAddDocument = { tNp, tEn, docNum, cat, authNp, authEn, issDate, expDate, st, ptsNp, ptsEn, officer, nts ->
                                viewModel.addOfficialDocument(tNp, tEn, docNum, cat, authNp, authEn, issDate, expDate, st, ptsNp, ptsEn, officer, nts)
                            },
                            onUpdateDocument = { doc -> viewModel.updateOfficialDocument(doc) },
                            onDeleteDocument = { id -> viewModel.deleteOfficialDocument(id) },
                            onRenewDocument = { id, exp, nts -> viewModel.renewOfficialDocument(id, exp, nts) },
                            onBack = { viewModel.navigateTo(FarmScreen.INVENTORY) }
                        )
                    }

                    FarmScreen.ROOM_INVENTORY -> {
                        InventoryItemListScreen(
                            items = inventoryItems,
                            isEnglish = isEnglish,
                            onAddItem = { newItem -> viewModel.addInventoryItem(newItem) },
                            onUpdateQuantity = { id, qty -> viewModel.updateInventoryItemQuantity(id, qty) },
                            onDeleteItem = { id -> viewModel.deleteInventoryItem(id) },
                            onBack = { viewModel.navigateTo(FarmScreen.INVENTORY) }
                        )
                    }

                    FarmScreen.CREATE_ORDER -> {
                        CreateOrderScreen(
                            inventoryItems = inventoryItems,
                            isEnglish = isEnglish,
                            onOrderCreated = { newOrder, paymentMode ->
                                viewModel.submitRoomOrder(newOrder)
                                if (paymentMode == "MUKTINATH_BANK") {
                                    viewModel.navigateTo(FarmScreen.MUKTINATH_PAYMENT)
                                } else {
                                    viewModel.navigateTo(FarmScreen.PAST_ORDERS)
                                }
                            },
                            onBack = { viewModel.navigateTo(FarmScreen.PAST_ORDERS) }
                        )
                    }

                    FarmScreen.MUKTINATH_PAYMENT -> {
                        val orderForPayment = activePaymentOrder ?: roomOrders.firstOrNull() ?: com.example.data.local.Order(
                            customerName = "Customer",
                            customerPhone = "9841000000",
                            totalAmount = 2500.0,
                            status = "PENDING_VERIFICATION"
                        )
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(if (isEnglish) "Muktinath Bank Payment" else "मुक्तिनाथ बैंक भुक्तानी", color = Color.White) },
                                    navigationIcon = {
                                        IconButton(onClick = { viewModel.navigateTo(FarmScreen.ORDERS) }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AgroGreenPrimary)
                                )
                            }
                        ) { payPadding ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(payPadding)
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                MuktinathPaymentRequestCard(
                                    order = orderForPayment,
                                    isEnglish = isEnglish,
                                    onPaymentSubmitted = { sender, ref, amt, voucher ->
                                        viewModel.updateRoomOrderStatus(orderForPayment.orderId, "PENDING_VERIFICATION")
                                        if (voucher != null) {
                                            viewModel.attachDocument(
                                                AttachedDocumentItem(
                                                    title = "Voucher #$ref",
                                                    category = "PAYMENT_RECEIPT",
                                                    uriString = voucher,
                                                    fileName = "voucher_${orderForPayment.orderId}.jpg",
                                                    fileSizeFormatted = "Image voucher",
                                                    isImage = true
                                                )
                                            )
                                        }
                                    },
                                    onVerifyPayment = { id ->
                                        viewModel.updateRoomOrderStatus(id, "VERIFIED")
                                    }
                                )
                            }
                        }
                    }

                    FarmScreen.PAST_ORDERS -> {
                        PastOrdersScreen(
                            orders = roomOrders,
                            isEnglish = isEnglish,
                            onSelectOrder = { order ->
                                // Order selected
                            },
                            onUpdateStatus = { orderId, status ->
                                viewModel.updateRoomOrderStatus(orderId, status)
                            },
                            onDeleteOrder = { orderId ->
                                viewModel.deleteRoomOrder(orderId)
                            },
                            onNavigateToCreateOrder = {
                                viewModel.navigateTo(FarmScreen.CREATE_ORDER)
                            },
                            onNavigateToMuktinathPayment = { order ->
                                viewModel.setActivePaymentOrder(order)
                                viewModel.navigateTo(FarmScreen.MUKTINATH_PAYMENT)
                            },
                            onNavigateToRevenueChart = {
                                viewModel.navigateTo(FarmScreen.MONTHLY_REVENUE)
                            },
                            onBack = {
                                viewModel.navigateTo(FarmScreen.ORDERS)
                            }
                        )
                    }

                    FarmScreen.MONTHLY_REVENUE -> {
                        MonthlyRevenueScreen(
                            orders = roomOrders,
                            isEnglish = isEnglish,
                            onBack = {
                                viewModel.navigateTo(FarmScreen.PAST_ORDERS)
                            },
                            onNavigateToPastOrders = {
                                viewModel.navigateTo(FarmScreen.PAST_ORDERS)
                            },
                            onNavigateToCreateOrder = {
                                viewModel.navigateTo(FarmScreen.CREATE_ORDER)
                            },
                            onSeedSampleOrders = {
                                viewModel.seedMultiMonthOrders()
                            }
                        )
                    }
                }

                // Document & Company Image Upload Dialog
                DocumentUploadMenuDialog(
                    isOpen = showUploadMenuDialog,
                    isEnglish = isEnglish,
                    onDismiss = { showUploadMenuDialog = false },
                    onDocumentAttached = { doc ->
                        viewModel.attachDocument(doc)
                    }
                )

                // Order Receipt Dialog
                selectedOrderForReceipt?.let { order ->
                    OrderReceiptDialog(
                        order = order,
                        isEnglish = isEnglish,
                        onDismiss = { viewModel.closeReceipt() },
                        onVerifyClick = { id ->
                            viewModel.verifyOrderPayment(id)
                            viewModel.closeReceipt()
                        }
                    )
                }

                // User Profile & Security Dialog
                if (showProfileDialog) {
                    AlertDialog(
                        onDismissRequest = { showProfileDialog = false },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = AgroGreenPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isEnglish) "User Profile & Security" else "प्रयोगकर्ता प्रोफाइल तथा सुरक्षा",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = currentUser?.displayName ?: "Farm User",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F1F1F)
                                )
                                Text(
                                    text = currentUser?.email ?: "No email associated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = AgroGreenPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEnglish) "Firebase Auth: Verified" else "फायरबेस अथेन्टिकेसन: प्रमाणित",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = AgroGreenPrimary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF1565C0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEnglish) "Role: ${userProfile?.role?.labelEn ?: "Staff"}" else "पहुँच स्तर: ${userProfile?.role?.labelNp ?: "कर्मचारी / व्यवस्थापक"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1565C0)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isEnglish) "Muktinath Bikas Bank: Connected" else "मुक्तिनाथ विकास बैंक: जोडिएको छ",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFE65100)
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Text(
                                    text = if (isEnglish) "Switch Access Role:" else "पहुँच स्तर परिवर्तन गर्नुहोस्:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.syncUserProfile(UserRole.STAFF) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (userProfile?.role == UserRole.STAFF) AgroGreenPrimary else Color(0xFFEEEEEE),
                                            contentColor = if (userProfile?.role == UserRole.STAFF) Color.White else Color(0xFF424242)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (isEnglish) "Staff" else "व्यवस्थापक", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.syncUserProfile(UserRole.CUSTOMER) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (userProfile?.role == UserRole.CUSTOMER) AgroGreenPrimary else Color(0xFFEEEEEE),
                                            contentColor = if (userProfile?.role == UserRole.CUSTOMER) Color.White else Color(0xFF424242)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (isEnglish) "Customer" else "ग्राहक", fontSize = 12.sp)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showProfileDialog = false
                                    onSignOut()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                modifier = Modifier.testTag("dialog_sign_out_button")
                            ) {
                                Text(if (isEnglish) "Sign Out" else "लगआउट गर्नुहोस्")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showProfileDialog = false }) {
                                Text(if (isEnglish) "Close" else "बन्द")
                            }
                        }
                    )
                }
            }
        }
    }
}
