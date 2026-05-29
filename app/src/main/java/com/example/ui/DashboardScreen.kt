package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppSettings
import com.example.data.Transaction
import com.example.ui.theme.*
import com.example.viewmodel.KycState
import com.example.viewmodel.PaymentState
import com.example.viewmodel.WalletViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val kycProgress by viewModel.kycProgress.collectAsStateWithLifecycle()
    val paymentState by viewModel.paymentState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(0) } // 0: Home/UPI, 1: Card, 2: Analytics, 3: Parent
    var showScanQrModal by remember { mutableStateOf(false) }
    var showKycModal by remember { mutableStateOf(false) }
    var showAddMoneyModal by remember { mutableStateOf(false) }
    var showPayUpiModal by remember { mutableStateOf(false) }

    // Safe settings fallback to avoid blank screens while loading
    val activeSettings = settings ?: AppSettings()

    // Screen-level background
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSlateBg,
                    titleContentColor = Color.White
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Custom Brand Circle
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(NeonYellow, NeonTeal)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "M",
                                color = JetBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Column {
                            Text(
                                "FamM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (activeSettings.kycVerified) NeonTeal else NeonOrange)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (activeSettings.kycVerified) "Aadhar Verified" else "KYC Pending",
                                    fontSize = 11.sp,
                                    color = if (activeSettings.kycVerified) NeonTeal else NeonOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Quick top-up icon
                    IconButton(
                        onClick = { showAddMoneyModal = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkGreySurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pocket Money addition",
                            tint = NeonYellow
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Profile/User visual
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CardPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeSettings.cardHolderName.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        },
        bottomBar = {
            CustomBottomNavigation(
                selectedIndex = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        containerColor = DarkSlateBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main page transition
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                }
            ) { targetTab ->
                when (targetTab) {
                    0 -> HomeTabScreen(
                        settings = activeSettings,
                        transactions = transactions,
                        onScanQrClicked = { showScanQrModal = true },
                        onPayUpiClicked = { showPayUpiModal = true },
                        onAddPercentClicked = { showAddMoneyModal = true },
                        onVerifyKycClicked = { showKycModal = true }
                    )
                    1 -> CardTabScreen(
                        settings = activeSettings,
                        viewModel = viewModel
                    )
                    2 -> AnalyticsTabScreen(
                        settings = activeSettings,
                        transactions = transactions
                    )
                    3 -> ParentTabScreen(
                        settings = activeSettings,
                        viewModel = viewModel
                    )
                }
            }

            // --- ALL DIALOGS & OVERLAYS ---

            // Payment State Dialog Observer
            if (paymentState != PaymentState.Idle) {
                PaymentStatusDialog(
                    state = paymentState,
                    onDismiss = { viewModel.resetPaymentState() },
                    onApproveParent = { viewModel.approveParentalPending() },
                    onDeclineParent = { viewModel.rejectParentalPending() }
                )
            }

            // Scan QR Code Dialog Simulator
            if (showScanQrModal) {
                ScanQrDialog(
                    onDismiss = { showScanQrModal = false },
                    onConfirmPayment = { targetMerchant, amount, category ->
                        showScanQrModal = false
                        viewModel.initiatePayment(targetMerchant, amount, category)
                    }
                )
            }

            // Pay via UPI ID dialog simulator
            if (showPayUpiModal) {
                PayUpiDialog(
                    onDismiss = { showPayUpiModal = false },
                    onConfirmPayment = { targetUpi, amount ->
                        showPayUpiModal = false
                        viewModel.initiatePayment("UPI to $targetUpi", amount, "Others")
                    }
                )
            }

            // Secure Aadhar KYC Dialog Simulator
            if (showKycModal) {
                AadharKycDialog(
                    state = kycProgress,
                    onDismiss = {
                        showKycModal = false
                        viewModel.resetKycState()
                    },
                    onRequestOtp = { aadharNo ->
                        viewModel.startKyc(aadharNo)
                    },
                    onVerifyOtp = { otpCode ->
                        viewModel.verifyKycOtp(otpCode)
                    }
                )
            }

            // Simulated Parent Top Up Pocket Money Modal
            if (showAddMoneyModal) {
                AddMoneyDialog(
                    onDismiss = { showAddMoneyModal = false },
                    onConfirmAdd = { amount ->
                        showAddMoneyModal = false
                        viewModel.addPocketMoney(amount)
                    }
                )
            }
        }
    }
}

// ==========================================
// CUSTOM NAVIGATION BAR
// ==========================================
@Composable
fun CustomBottomNavigation(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        color = DarkSlateBg,
        tonalElevation = 8.dp
    ) {
        Column {
            HorizontalDivider(color = BorderGrey, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = selectedIndex == 0,
                    onClick = { onTabSelected(0) },
                    modifier = Modifier.testTag("nav_home")
                )
                BottomNavItem(
                    label = "FamM Card",
                    icon = Icons.Default.CreditCard,
                    isSelected = selectedIndex == 1,
                    onClick = { onTabSelected(1) },
                    modifier = Modifier.testTag("nav_card")
                )
                BottomNavItem(
                    label = "Budget",
                    icon = Icons.Default.Assessment, // Bar chart indicator
                    isSelected = selectedIndex == 2,
                    onClick = { onTabSelected(2) },
                    modifier = Modifier.testTag("nav_analytics")
                )
                BottomNavItem(
                    label = "Parent Guard",
                    icon = Icons.Default.Security,
                    isSelected = selectedIndex == 3,
                    onClick = { onTabSelected(3) },
                    modifier = Modifier.testTag("nav_parent")
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val duration = 250
    val activeColor = NeonYellow
    val inactiveColor = AccentGrey

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(duration)
    )

    Column(
        modifier = modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) DarkGreySurface else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) activeColor else inactiveColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) activeColor else inactiveColor
                )
            }
        }
    }
}

// ==========================================
// SCREEN: HOME / WALLET SCREEN
// ==========================================
@Composable
fun HomeTabScreen(
    settings: AppSettings,
    transactions: List<Transaction>,
    onScanQrClicked: () -> Unit,
    onPayUpiClicked: () -> Unit,
    onAddPercentClicked: () -> Unit,
    onVerifyKycClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // WALLET BALANCE CARD (Neon dynamic card with atmospheric background)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DarkGreySurface, JetBlack)
                        )
                    )
                    .border(1.dp, BorderGrey, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "YOUR DIGI-WALLET",
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "FREE UPI ALWAYS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonTeal
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "₹${String.format(Locale.US, "%,.2f", settings.walletBalance)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = "Contact info",
                            tint = NeonYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Daily limit remaining: ₹${String.format(Locale.US, "%.0f", (settings.spendingLimitDaily - settings.spendingSpentToday).coerceAtLeast(0.0))}",
                            fontSize = 11.sp,
                            color = AccentGrey
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pocket Action shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onScanQrClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_scan_qr_btn"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = JetBlack)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Qr", color = JetBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Button(
                            onClick = onPayUpiClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSlateBg),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderGrey),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_pay_upi_btn"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pay UPI ID", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // KYC PENDING WARNING / BENEFITS (Visible only if kycVerified is false)
        if (!settings.kycVerified) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonOrange.copy(alpha = 0.1f))
                        .border(1.dp, NeonOrange.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .clickable(onClick = onVerifyKycClicked)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentInd,
                                contentDescription = null,
                                tint = NeonOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Unlock Full Wallet Power!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Setup instant Aadhar KYC to lift limits and activate Tap & Pay.",
                                color = AccentGrey,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "KYC Arrow",
                            tint = NeonOrange
                        )
                    }
                }
            }
        }

        // FUN TEEN BONUS TILES
        item {
            Column {
                Text(
                    "TEEN OFFERS & REWARDS",
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGrey,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OfferCard(
                        title = "50% Gaming Cash",
                        subtitle = "On Steam & Epic Games payments",
                        colorStop1 = CardPurple,
                        colorStop2 = Color(0xFF482D99)
                    )
                    OfferCard(
                        title = "Free Pizza Slice",
                        subtitle = "Spend above ₹300 at Pizza Hut",
                        colorStop1 = CardRose,
                        colorStop2 = Color(0xFFC71E59)
                    )
                    OfferCard(
                        title = "Refer Your Gang",
                        subtitle = "Get ₹100 for every friend who joins",
                        colorStop1 = NeonTeal,
                        colorStop2 = Color(0xFF0C9790)
                    )
                }
            }
        }

        // TRANSACTION HISTORY DASHBOARD
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT TRANSACTIONS",
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGrey
                )
                Text(
                    text = "${transactions.size} records",
                    fontSize = 11.sp,
                    color = NeonTeal,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty transactions list",
                            tint = BorderGrey,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No transactions recorded yet.",
                            color = AccentGrey,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(transactions) { tx ->
                TransactionRowItem(tx)
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

@Composable
fun OfferCard(
    title: String,
    subtitle: String,
    colorStop1: Color,
    colorStop2: Color
) {
    Box(
        modifier = Modifier
            .width(170.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(colorStop1, colorStop2)
                )
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("HOT DEAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TransactionRowItem(tx: Transaction) {
    val categoryIcon = when (tx.category) {
        "Food" -> Icons.Default.Restaurant
        "Shopping" -> Icons.Default.LocalMall
        "Gaming" -> Icons.Default.SportsEsports
        "Entertainment" -> Icons.Default.Movie
        "Outings" -> Icons.Default.DirectionsCar
        else -> Icons.Default.Payments
    }

    val categoryColor = when (tx.category) {
        "Gaming" -> CardPurple
        "Food" -> NeonOrange
        "Shopping" -> NeonTeal
        "Entertainment" -> CardRose
        "Outings" -> NeonYellow
        else -> AccentGrey
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkGreySurface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon Circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categoryIcon,
                contentDescription = tx.category,
                tint = categoryColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.title,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp)),
                color = AccentGrey,
                fontSize = 9.sp
            )
        }

        // Amount & category badge
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (tx.isDebited) "-₹${tx.amount}" else "+₹${tx.amount}",
                fontWeight = FontWeight.Bold,
                color = if (tx.isDebited) Color.White else NeonTeal,
                fontSize = 14.sp
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BorderGrey)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tx.category,
                    fontSize = 8.sp,
                    color = AccentGrey,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ==========================================
// SCREEN: PHYSICAL / VIRTUAL CARD
// ==========================================
@Composable
fun CardTabScreen(
    settings: AppSettings,
    viewModel: WalletViewModel
) {
    var showCvv by remember { mutableStateOf(false) }
    var inputCardName by remember { mutableStateOf(settings.cardHolderName) }
    var isEditingCardName by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // EXQUISITE SHINY PHYSICAL/VIRTUAL CARD CANVAS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.586f) // Standard CR80 card aspect ratio
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (settings.cardBlocked) {
                                listOf(Color(0xFF222222), Color(0xFF111111))
                            } else {
                                listOf(Color(0xFF030508), Color(0xFF13171F), Color(0xFF07090C))
                            }
                        )
                    )
                    .border(
                        BorderStroke(
                            width = 1.5.dp,
                            brush = if (settings.cardBlocked) {
                                androidx.compose.ui.graphics.SolidColor(Color(0xFF333333))
                            } else {
                                Brush.linearGradient(
                                    colors = listOf(NeonYellow, NeonTeal, CardPurple)
                                )
                            }
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .drawBehind {
                        // Let's draw some faint, beautiful neon cyber lines for a technical high-end/premium pattern
                        if (!settings.cardBlocked) {
                            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            drawCircle(
                                color = NeonTeal.copy(alpha = 0.12f),
                                radius = size.width * 0.45f,
                                center = Offset(size.width * 0.85f, size.height * 0.15f)
                            )
                            drawCircle(
                                color = NeonYellow.copy(alpha = 0.08f),
                                radius = size.width * 0.3f,
                                center = Offset(size.width * 0.15f, size.height * 0.85f)
                            )
                            drawLine(
                                color = NeonTeal.copy(alpha = 0.15f),
                                start = Offset(0f, size.height * 0.35f),
                                end = Offset(size.width, size.height * 0.65f),
                                strokeWidth = 1.5f,
                                pathEffect = pathEffect
                            )
                            drawLine(
                                color = CardPurple.copy(alpha = 0.2f),
                                start = Offset(size.width * 0.25f, 0f),
                                end = Offset(size.width * 0.75f, size.height),
                                strokeWidth = 1.5f,
                                pathEffect = pathEffect
                            )
                        }
                    }
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "FamM Card",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                if (settings.cardBlocked) "BLOCKED" else "TAP & PAY ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Card chip visual representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Gold Chip
                        Box(
                            modifier = Modifier
                                .size(34.dp, 26.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE4BC52))
                        )

                        // Contactless signal
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Contactless tap enabled icon",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        // Secure Number
                        Text(
                            text = if (showCvv) settings.cardNo else "••••  ••••  ••••  ${settings.cardNo.takeLast(4)}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("CARDHOLDER", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f))
                                Text(
                                    text = settings.cardHolderName.uppercase(),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column {
                                    Text("EXPIRY", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text(settings.cardExpiry, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                                }
                                Column {
                                    Text("CVV", fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f))
                                    Text(
                                        text = if (showCvv) settings.cardCvvMock else "•••",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // VIEW SENSITIVE CVV BUTTON
        item {
            Button(
                onClick = { showCvv = !showCvv },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreySurface),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, BorderGrey),
                modifier = Modifier.testTag("toggle_cvv_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (showCvv) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "CVV Reveal",
                        tint = NeonYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (showCvv) "Hide Secret Details" else "Show Secret Details",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // CUSTOM NAME CUSTOMIZER SETTING
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "PERSONALIZE YOUR CARDNAME",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )
                    if (isEditingCardName) {
                        OutlinedTextField(
                            value = inputCardName,
                            onValueChange = { inputCardName = it },
                            label = { Text("Display Name on physical card") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonYellow,
                                focusedLabelColor = NeonYellow
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_holder_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.updateCardHolderName(inputCardName)
                                    isEditingCardName = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Save", color = JetBlack, fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = { isEditingCardName = false }) {
                                Text("Cancel", color = AccentGrey)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Active printed name:", fontSize = 11.sp, color = AccentGrey)
                                Text(settings.cardHolderName, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Button(
                                onClick = { isEditingCardName = true },
                                colors = ButtonDefaults.buttonColors(containerColor = BorderGrey),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("edit_card_name_btn")
                            ) {
                                Text("Change", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // PHYSICAL CARD CONTROLS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "SECURITY CONTROLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // 1. Lock Switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (settings.cardBlocked) NeonOrange.copy(alpha = 0.15f) else BorderGrey),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (settings.cardBlocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (settings.cardBlocked) NeonOrange else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text("Temporary Block Card", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("Stops all card & wallet payouts instantly", fontSize = 10.sp, color = AccentGrey)
                            }
                        }
                        Switch(
                            checked = settings.cardBlocked,
                            onCheckedChange = { viewModel.toggleCardBlockState() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JetBlack,
                                checkedTrackColor = NeonOrange,
                                uncheckedThumbColor = AccentGrey,
                                uncheckedTrackColor = BorderGrey
                            ),
                            modifier = Modifier.testTag("block_card_switch")
                        )
                    }

                    HorizontalDivider(color = BorderGrey)

                    // 2. Tap and pay toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (settings.cardTapEnabled) NeonTeal.copy(alpha = 0.15f) else BorderGrey),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contactless,
                                    contentDescription = "Contactless switch logo",
                                    tint = if (settings.cardTapEnabled) NeonTeal else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text("Tap & Pay (Contactless)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("Pay without pin up to ₹500", fontSize = 10.sp, color = AccentGrey)
                            }
                        }
                        Switch(
                            checked = settings.cardTapEnabled,
                            onCheckedChange = { viewModel.toggleCardTapAndPay() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JetBlack,
                                checkedTrackColor = NeonTeal,
                                uncheckedThumbColor = AccentGrey,
                                uncheckedTrackColor = BorderGrey
                            ),
                            modifier = Modifier.testTag("tap_pay_switch")
                        )
                    }
                }
            }
        }

        // PHYSICAL CARD DELIVERY TRAVEL TRACKER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "PHYSICAL FAMCARD TRANSIT STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )

                    // Delivery Tracker step map
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DeliveryStep(label = "Card Printed", completed = true, active = false)
                        DeliveryLine(completed = true)
                        DeliveryStep(label = "In Transit", completed = true, active = true)
                        DeliveryLine(completed = false)
                        DeliveryStep(label = "Out for Delivery", completed = false, active = false)
                        DeliveryLine(completed = false)
                        DeliveryStep(label = "Delivered", completed = false, active = false)
                    }

                    Text(
                        text = "Your personalized physical FamCard has left our warehouse and is in transit via BlueDart (Tracking ID: FM73109X). Estimated delivery in 2 days.",
                        fontSize = 10.sp,
                        color = AccentGrey,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun RowScope.DeliveryStep(label: String, completed: Boolean, active: Boolean) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (completed) {
                        if (active) NeonYellow else NeonTeal
                    } else {
                        BorderGrey
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (completed && !active) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = JetBlack,
                    modifier = Modifier.size(12.dp)
                )
            } else if (active) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(JetBlack)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (completed) Color.White else AccentGrey,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun RowScope.DeliveryLine(completed: Boolean) {
    Box(
        modifier = Modifier
            .weight(0.5f)
            .height(2.dp)
            .background(if (completed) NeonTeal else BorderGrey)
            .offset(y = (-6).dp)
    )
}

// ==========================================
// SCREEN: EXPENSES & BUDGET ANALYSIS
// ==========================================
@Composable
fun AnalyticsTabScreen(
    settings: AppSettings,
    transactions: List<Transaction>
) {
    // Group transactions by category to find spent amount
    val categoryTotals = remember(transactions) {
        val totals = mutableMapOf(
            "Food" to 0.0,
            "Shopping" to 0.0,
            "Gaming" to 0.0,
            "Entertainment" to 0.0,
            "Outings" to 0.0,
            "Others" to 0.0
        )
        transactions.filter { it.isDebited && it.status == "SUCCESS" }.forEach {
            val key = it.category
            totals[key] = (totals[key] ?: 0.0) + it.amount
        }
        totals
    }

    val totalSpent = remember(categoryTotals) {
        categoryTotals.values.sum()
    }

    // High target static or preset (e.g., set dynamically in our simulator settings)
    val monthlyTargetBudget = 5000.0 // preset simulation budget target
    val budgetPercent = (totalSpent / monthlyTargetBudget).toFloat().coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // EXQUISITE MONTHLY BUDGET RADIAL HEAT DIAL / STATE
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, BorderGrey, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "TEEN SPENDING METER",
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Radial Speedometer / Progress Bar using custom Canvas
                    Box(
                        modifier = Modifier
                            .size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(150.dp)) {
                            // Track
                            drawArc(
                                color = BorderGrey,
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), pathEffect = PathEffect.cornerPathEffect(4f))
                            )
                            // Progress Brush
                            val progressStroke = Brush.linearGradient(
                                colors = when {
                                    budgetPercent > 0.8f -> listOf(NeonOrange, Color.Red)
                                    budgetPercent > 0.5f -> listOf(NeonYellow, NeonOrange)
                                    else -> listOf(NeonTeal, NeonYellow)
                                }
                            )
                            drawArc(
                                brush = progressStroke,
                                startAngle = 135f,
                                sweepAngle = 270f * budgetPercent,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), pathEffect = PathEffect.cornerPathEffect(4f))
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Total Spent",
                                fontSize = 11.sp,
                                color = AccentGrey
                            )
                            Text(
                                "₹${String.format(Locale.US, "%,.0f", totalSpent)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Limit: ₹${String.format(Locale.US, "%,.0f", monthlyTargetBudget)}",
                                fontSize = 11.sp,
                                color = AccentGrey
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Budget warning system text
                    val (statusText, statusColor) = when {
                        budgetPercent >= 1.0f -> "CRITICAL OVERRUN: You have completely drained your preset monthly threshold!" to Color.Red
                        budgetPercent > 0.8f -> "SQUEEZE BUDGET: You've spent over 80%! Cool off shopping now." to NeonOrange
                        budgetPercent > 0.5f -> "MEDIUM TRACKS: Pocket money is halfway finished. Spend wisely." to NeonYellow
                        else -> "SAFE SPENDING ZONE: Your expense discipline is outstanding." to NeonTeal
                    }
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // BAR CHART CATEGORY VISUALS
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "CATEGORY WISE EXPENSES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )

                    val maxCatValue = categoryTotals.values.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0

                    categoryTotals.forEach { (cat, valSum) ->
                        val ratio = (valSum / maxCatValue).toFloat().coerceIn(0f, 1f)
                        val catColor = when (cat) {
                            "Gaming" -> CardPurple
                            "Food" -> NeonOrange
                            "Shopping" -> NeonTeal
                            "Entertainment" -> CardRose
                            "Outings" -> NeonYellow
                            else -> AccentGrey
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                    Text(cat, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text("₹${String.format(Locale.US, "%,.0f", valSum)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            // Custom progress track
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(BorderGrey)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(ratio)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(catColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ==========================================
// SCREEN: PARENT PORTAL / PARENTAL CONTROL
// ==========================================
@Composable
fun ParentTabScreen(
    settings: AppSettings,
    viewModel: WalletViewModel
) {
    var limitSliderValue by remember { mutableStateOf(settings.spendingLimitDaily.toFloat()) }
    var parentMobile by remember { mutableStateOf(settings.parentLinkedContact) }
    var autoChecked by remember { mutableStateOf(settings.parentalApprovalRequired) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // MAIN DESCRIPTION BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardPurple.copy(alpha = 0.15f))
                    .border(1.dp, CardPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(24.dp))
                        Text(
                            "Parent Guard® Safety Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                    }
                    Text(
                        "Parent guard allows legal guardians to link their mobile, setting spending limitations and approving any high-value transactions instantly.",
                        color = AccentGrey,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // DAILY LIMIT CONTROL SLIDER (Simulating parent adjustment)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "DAILY SPENDING THRESHOLD",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonTeal
                        )
                        Text(
                            "₹${String.format(Locale.US, "%,.0f", limitSliderValue)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonYellow
                        )
                    }

                    Slider(
                        value = limitSliderValue,
                        onValueChange = { limitSliderValue = it },
                        valueRange = 100f..5000f,
                        steps = 49,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonYellow,
                            activeTrackColor = NeonYellow,
                            inactiveTrackColor = BorderGrey
                        ),
                        modifier = Modifier.testTag("parent_limit_slider")
                    )

                    Text(
                        "Drag slider to setting threshold limit. Changes take effect on teenager's digital wallet in real time.",
                        fontSize = 10.sp,
                        color = AccentGrey
                    )

                    Button(
                        onClick = { viewModel.updateDailyLimit(limitSliderValue.toDouble()) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_parent_limit_btn")
                    ) {
                        Text("Apply Teen's Limit", color = JetBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // PARENT PHONE & APPROVAL TAPE
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "AUTHENTICATION MECHANISM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )

                    // Parental Approval Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Approval Alert (> ₹500)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Requires instant passcode override/approval for single transactions above ₹500", fontSize = 10.sp, color = AccentGrey)
                        }
                        Switch(
                            checked = autoChecked,
                            onCheckedChange = { autoChecked = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JetBlack,
                                checkedTrackColor = NeonTeal,
                                uncheckedThumbColor = AccentGrey,
                                uncheckedTrackColor = BorderGrey
                            ),
                            modifier = Modifier.testTag("approval_alert_switch")
                        )
                    }

                    HorizontalDivider(color = BorderGrey)

                    // Phone input
                    OutlinedTextField(
                        value = parentMobile,
                        onValueChange = { parentMobile = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        label = { Text("Parent Linked Phone Number") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = NeonTeal,
                            focusedLabelColor = NeonTeal
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_phone_input")
                    )

                    Button(
                        onClick = { viewModel.updateParentalSettings(autoChecked, parentMobile) },
                        colors = ButtonDefaults.buttonColors(containerColor = BorderGrey),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_parental_settings_btn")
                    ) {
                        Text("Save Parental Contacts", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        // SIMULATED ADD MONEY CORNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkGreySurface)
                    .border(1.dp, BorderGrey, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "SIMULATED PARENT TRANSFERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonTeal
                    )
                    Text(
                        "Click buttons below to simulate a parent's positive UPI transfer of pocket money or manual budget resetting.",
                        fontSize = 11.sp,
                        color = AccentGrey
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.addPocketMoney(1000.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = CardPurple),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sim_add_1000"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ ₹1,000 Wallet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.resetDailySpending() },
                            colors = ButtonDefaults.buttonColors(containerColor = BorderGrey),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sim_reset_daily"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset Today's Spend", color = NeonTeal, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// ==========================================
// SIMULATOR COMPOSABLES: DIALOGS & POPUPS
// ==========================================

// 1. SCAN QR SIMULATOR
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrDialog(
    onDismiss: () -> Unit,
    onConfirmPayment: (String, Double, String) -> Unit
) {
    var customMerchant by remember { mutableStateOf("") }
    var customAmount by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            color = DarkSlateBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scan QR Code", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AccentGrey)
                    }
                }

                // GLOWING VIEW FINDER REPRESENTATION
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(JetBlack)
                        .border(2.dp, NeonYellow, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Moving green scanning line simulation
                    val infiniteTransition = rememberInfiniteTransition()
                    val greenLineOffset by infiniteTransition.animateFloat(
                        initialValue = 10f,
                        targetValue = 170f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(
                            color = NeonTeal,
                            start = Offset(10f, greenLineOffset),
                            end = Offset(size.width - 10f, greenLineOffset),
                            strokeWidth = 3.dp.toPx()
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Simulated Scan",
                        tint = NeonYellow.copy(alpha = 0.5f),
                        modifier = Modifier.size(100.dp)
                    )
                }

                Text(
                    "Simulated Scanner actively pointing at merchant tags...",
                    fontSize = 11.sp,
                    color = AccentGrey,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                HorizontalDivider(color = BorderGrey)

                // MERCHANTS TO INSTANT TRANSFER
                Text("TAP A SIMULATED MERCHANT IN YOUR RANGE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonTeal, modifier = Modifier.align(Alignment.Start))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MerchantTag(name = "Pizza Hut", amt = 350.0, onClick = { onConfirmPayment("Pizza Hut Pizza", 350.0, "Food") })
                    MerchantTag(name = "Steam Wallet", amt = 450.0, onClick = { onConfirmPayment("Steam Game Wallet", 450.0, "Gaming") })
                    MerchantTag(name = "Cafe Coffee", amt = 120.0, onClick = { onConfirmPayment("CCD Cappuccino Cafe", 120.0, "Food") })
                    MerchantTag(name = "Decathlon Store", amt = 999.0, onClick = { onConfirmPayment("Decathlon Fitness Gear", 999.0, "Shopping") })
                }

                HorizontalDivider(color = BorderGrey)

                // CUSTOM QUICK ENTER MERCHANT
                Text("OR INPUT MANUAL MERCHANT DATA:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonTeal, modifier = Modifier.align(Alignment.Start))

                OutlinedTextField(
                    value = customMerchant,
                    onValueChange = { customMerchant = it },
                    label = { Text("Shop Name (Details)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonYellow,
                        focusedLabelColor = NeonYellow
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_merchant_name")
                )

                OutlinedTextField(
                    value = customAmount,
                    onValueChange = { customAmount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("Transfer Cash (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonYellow,
                        focusedLabelColor = NeonYellow
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qr_merchant_amount")
                )

                Button(
                    onClick = {
                        val amt = customAmount.toDoubleOrNull()
                        if (customMerchant.isNotBlank() && amt != null && amt > 0) {
                            onConfirmPayment(customMerchant, amt, "Others")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("qr_pay_instant_btn")
                ) {
                    Text("Pay Now ₹${customAmount.ifBlank { "0" }} Free", color = JetBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MerchantTag(name: String, amt: Double, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkGreySurface)
            .border(1.dp, BorderGrey, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text("₹$amt", color = NeonYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// 2. PAY ID UPI DIALOG
@Composable
fun PayUpiDialog(
    onDismiss: () -> Unit,
    onConfirmPayment: (String, Double) -> Unit
) {
    var upiIdInput by remember { mutableStateOf("") }
    var upiAmountInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = DarkSlateBg,
            border = BorderStroke(1.dp, BorderGrey)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pay Any UPI ID", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AccentGrey)
                    }
                }

                OutlinedTextField(
                    value = upiIdInput,
                    onValueChange = { upiIdInput = it },
                    placeholder = { Text("e.g. friend@famm") },
                    label = { Text("UPI ID or Linked Phone No.") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonYellow,
                        focusedLabelColor = NeonYellow
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_target_id")
                )

                OutlinedTextField(
                    value = upiAmountInput,
                    onValueChange = { upiAmountInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("0.00") },
                    label = { Text("Transfer Cash Amount (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonYellow,
                        focusedLabelColor = NeonYellow
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_target_amount")
                )

                Button(
                    onClick = {
                        val amount = upiAmountInput.toDoubleOrNull()
                        if (upiIdInput.isNotBlank() && amount != null && amount > 0) {
                            onConfirmPayment(upiIdInput, amount)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("upi_pay_confirm_btn")
                ) {
                    Text("Proceed securely (No UPI Fee)", color = JetBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 3. AADHAR KYC DIALOG
@Composable
fun AadharKycDialog(
    state: KycState,
    onDismiss: () -> Unit,
    onRequestOtp: (String) -> Unit,
    onVerifyOtp: (String) -> Unit
) {
    var aadharValue by remember { mutableStateOf("") }
    var otpValue by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = DarkSlateBg,
            border = BorderStroke(1.dp, BorderGrey)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Secure UIDAI Aadhar verification", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AccentGrey)
                    }
                }

                when (state) {
                    is KycState.Initial -> {
                        Icon(Icons.Default.AssignmentInd, contentDescription = null, tint = NeonYellow, modifier = Modifier.size(54.dp))
                        Text(
                            "Fast & Secure Verification process by OTP connected securely to UIDAI register.",
                            fontSize = 11.sp,
                            color = AccentGrey,
                            textAlign = TextAlign.Center
                        )
                        OutlinedTextField(
                            value = aadharValue,
                            onValueChange = { if (it.length <= 12) aadharValue = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("12-digit numeric key") },
                            label = { Text("Your Aadhar Card Number") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonYellow,
                                focusedLabelColor = NeonYellow
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("aadhar_input_field")
                        )
                        Button(
                            onClick = { onRequestOtp(aadharValue) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                            shape = RoundedCornerShape(10.dp),
                            enabled = aadharValue.length == 12,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("aadhar_submit_btn")
                        ) {
                            Text("Send Verification OTP", color = JetBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                    is KycState.SendingOtp -> {
                        CircularProgressIndicator(color = NeonTeal)
                        Text("Sending verification OTP connected with parent...", color = Color.White, fontSize = 13.sp)
                    }
                    is KycState.OtpSent -> {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(54.dp))
                        Text(
                            "An OTP has been sent. Type 123456 to mock verification.",
                            color = AccentGrey,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        OutlinedTextField(
                            value = otpValue,
                            onValueChange = { otpValue = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("Type 123456") },
                            label = { Text("Enter 6-Digit OTP Code") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = NeonTeal,
                                focusedLabelColor = NeonTeal
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("aadhar_otp_input")
                        )
                        Button(
                            onClick = { onVerifyOtp(otpValue) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("aadhar_otp_submit_btn")
                        ) {
                            Text("Verify OTP Pin", color = JetBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                    is KycState.Verifying -> {
                        CircularProgressIndicator(color = NeonTeal)
                        Text("UIDAI matches database records...", color = Color.White, fontSize = 13.sp)
                    }
                    is KycState.Success -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(54.dp))
                        Text(
                            "Instant KYC Activated Successfully!",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            "Limits successfully unlocked up to ₹10,000 wallet status. Virtual card payment active.",
                            fontSize = 11.sp,
                            color = AccentGrey,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Awesome", color = JetBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                    is KycState.Error -> {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(54.dp))
                        Text(state.message, color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = BorderGrey),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry Verification", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// 4. ADD SIMULATED POCKET MONEY DIALOG
@Composable
fun AddMoneyDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = DarkSlateBg,
            border = BorderStroke(1.dp, BorderGrey)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Pocket Cash", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AccentGrey)
                    }
                }

                Text(
                    "Teen pocket money is added by linked parents instantly via UPI authorization or simple direct topups.",
                    fontSize = 11.sp,
                    color = AccentGrey
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = { amountText = "200" }, colors = ButtonDefaults.buttonColors(containerColor = BorderGrey), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) { Text("+₹200", color = Color.White, fontSize = 11.sp) }
                    Button(onClick = { amountText = "500" }, colors = ButtonDefaults.buttonColors(containerColor = BorderGrey), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) { Text("+₹500", color = Color.White, fontSize = 11.sp) }
                    Button(onClick = { amountText = "1000" }, colors = ButtonDefaults.buttonColors(containerColor = BorderGrey), shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) { Text("+₹1,000", color = Color.White, fontSize = 11.sp) }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("0.00") },
                    label = { Text("Amount of money (₹)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = NeonYellow,
                        focusedLabelColor = NeonYellow
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_money_input")
                )

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            onConfirmAdd(amt)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_money_confirm_btn")
                ) {
                    Text("Add Money", color = JetBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 5. PARENTAL / LIMIT / SUCCESS PAYMENT STATUS DIALOG
@Composable
fun PaymentStatusDialog(
    state: PaymentState,
    onDismiss: () -> Unit,
    onApproveParent: () -> Unit,
    onDeclineParent: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = DarkSlateBg,
            border = BorderStroke(1.dp, BorderGrey)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is PaymentState.LimitExceeded -> {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(54.dp))
                        Text("Daily Spend Limit Exceeded!", fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                        Text(
                            text = "This payment requires ₹${state.required} today, but your parent set limit is ₹${state.maxAllowed}.\n\nLinked parent will be notified to raise limits.",
                            color = AccentGrey,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = BorderGrey),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dismiss", color = Color.White)
                        }
                    }
                    is PaymentState.ParentalApprovalPending -> {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(54.dp))
                        Text("Parent Pending Override!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Text(
                            text = "Transaction of ₹${state.amount} to '${state.title}' exceeds non-alert ₹500 rule.\n\nSimulating parent app confirmation on parent terminal device.",
                            color = AccentGrey,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(color = BorderGrey)

                        // SIMULATE PARENT OVERRIDE OPTION
                        Text("SIMULATED PARENT TERMINAL ACTIONS:", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = NeonOrange)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onApproveParent,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("parent_sim_approve_btn")
                            ) {
                                Text("Approve", color = JetBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = onDeclineParent,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("parent_sim_decline_btn")
                            ) {
                                Text("Reject", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                    is PaymentState.Processing -> {
                        CircularProgressIndicator(color = NeonYellow)
                        Text("Processing UPI Instant Transfer...", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    is PaymentState.Success -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonTeal, modifier = Modifier.size(54.dp))
                        Text("Transaction Successful!", fontWeight = FontWeight.Black, color = NeonTeal, fontSize = 18.sp)
                        Text("Completed at ₹0.00 Zero Transaction Fees.", color = AccentGrey, fontSize = 11.sp)
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("success_confirm")
                        ) {
                            Text("Done", color = JetBlack, fontWeight = FontWeight.Bold)
                        }
                    }
                    is PaymentState.Error -> {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(54.dp))
                        Text("Payment Failed", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 16.sp)
                        Text(state.message, color = AccentGrey, fontSize = 11.sp, textAlign = TextAlign.Center)
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = BorderGrey),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Back", color = Color.White)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
