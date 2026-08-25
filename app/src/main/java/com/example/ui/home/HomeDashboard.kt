package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.Transaction
import com.example.ui.components.FrostedBottomBar
import com.example.ui.components.GlassTextField
import com.example.ui.components.GradientButton
import com.example.ui.components.SecondaryGlassButton
import com.example.ui.theme.*
import com.example.ui.transactions.TransactionDetailDialog
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeDashboard(
    viewModel: HomeViewModel,
    currentRoute: String,
    onNavigateBottomBar: (String) -> Unit,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToSeeAll: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val balance by viewModel.totalBalance.collectAsState()
    val income by viewModel.totalIncome.collectAsState()
    val expense by viewModel.totalExpense.collectAsState()

    var selectedTxForDetails by remember { mutableStateOf<Transaction?>(null) }

    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val balanceStr = format.format(balance)
    val incomeStr = format.format(income)
    val expenseStr = format.format(expense)

    val firstName = user?.fullName?.substringBefore(" ")?.takeIf { it.isNotBlank() } ?: "Friend"
    val initials = user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercase() }?.joinToString("")?.take(2)?.ifBlank { "U" } ?: "U"

    // Time of day greeting
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    val todayStr = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DarkBackground1,
                        DarkBackground2,
                        DarkBackground1
                    )
                )
            )
    ) {
        // Decorative atmospheric background glow circles
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-80).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        listOf(PrimaryEmerald.copy(alpha = 0.08f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = 120.dp)
                .background(
                    Brush.radialGradient(
                        listOf(PrimaryTeal.copy(alpha = 0.06f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Main Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 90.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryEmerald, PrimaryTeal)
                                )
                            )
                            .border(1.5.dp, GlassCardBorder, CircleShape)
                            .clickable { onNavigateBottomBar("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color(0xFF090D16),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "$greeting, $firstName 👋",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = todayStr,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Quick Scan Receipt Header Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SlateCardElevated)
                        .border(1.dp, GlassCardBorder, CircleShape)
                        .clickable { onNavigateToScanner() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = "Scan Receipt",
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Total Balance Card (Luxury Metallic Glass Finish)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFF000000))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF192233),
                                    Color(0xFF131A29),
                                    Color(0xFF16233B)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(
                                    PrimaryEmerald.copy(alpha = 0.4f),
                                    Color.White.copy(alpha = 0.1f),
                                    PrimaryTeal.copy(alpha = 0.2f)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL BALANCE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.2.sp,
                                color = TextSecondary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = "✨", fontSize = 10.sp)
                                    Text(
                                        text = "Live",
                                        color = PrimaryEmerald,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = balanceStr,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Income & Expense Breakdown Pills with 3D Icons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Income Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0F1824))
                                    .border(1.dp, IncomeGreen.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreenBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "📈", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Income", fontSize = 11.sp, color = TextSecondary)
                                        Text(
                                            incomeStr,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IncomeGreen
                                        )
                                    }
                                }
                            }

                            // Expense Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0F1824))
                                    .border(1.dp, ExpenseRed.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRedBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "📉", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Expense", fontSize = 11.sp, color = TextSecondary)
                                        Text(
                                            expenseStr,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // High-Utility Action CTAs (Add Transaction & Scan)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Primary Add Transaction Button
                            GradientButton(
                                text = "Add Entry",
                                onClick = onNavigateToAddTransaction,
                                modifier = Modifier.weight(1f),
                                height = 46.dp,
                                icon = {
                                    Text(text = "➕", fontSize = 15.sp)
                                }
                            )

                            // Scan Receipt Button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SlateCardElevated)
                                    .border(1.dp, GlassCardBorder, RoundedCornerShape(16.dp))
                                    .clickable(onClick = onNavigateToScanner)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = "🧾", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Scan Bill",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Recent Transactions Header & Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SlateCardElevated)
                                .clickable { onNavigateToSeeAll() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "See All →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEmerald
                            )
                        }
                    }

                    if (transactions.isEmpty()) {
                        // Polished Empty State Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SlateCardSurface)
                                .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(SlateCardElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = "No Transactions Logged Yet",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Add your first expense or scan a receipt to start tracking.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                GradientButton(
                                    text = "+ Add First Transaction",
                                    onClick = onNavigateToAddTransaction,
                                    height = 42.dp
                                )
                            }
                        }
                    } else {
                        // Transactions List (Latest 5 items)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            transactions.take(5).forEach { tx ->
                                val isExp = tx.type.equals("EXPENSE", ignoreCase = true)
                                val icon = when (tx.category.lowercase()) {
                                    "food" -> "🍔"
                                    "transport" -> "🚕"
                                    "shopping" -> "🛍️"
                                    "bills" -> "🧾"
                                    "health" -> "💊"
                                    "entertainment" -> "🍿"
                                    "education" -> "📚"
                                    "groceries" -> "🛒"
                                    "travel" -> "✈️"
                                    "salary" -> "💼"
                                    "freelance" -> "💻"
                                    "investment" -> "📈"
                                    "business" -> "🏢"
                                    "bonus" -> "🎁"
                                    "dividends" -> "💰"
                                    "rental" -> "🏠"
                                    "refund" -> "🔄"
                                    "cashback" -> "💳"
                                    else -> if (isExp) "💸" else "💵"
                                }
                                val colorBg = when (tx.category.lowercase()) {
                                    "food" -> FoodColor
                                    "transport" -> TransportColor
                                    "shopping" -> ShoppingColor
                                    "bills" -> BillsColor
                                    "health" -> HealthColor
                                    "entertainment" -> EntertainmentColor
                                    "education" -> EducationColor
                                    else -> if (isExp) ExpenseRed else IncomeGreen
                                }
                                val sign = if (isExp) "-" else "+"
                                val colorText = if (isExp) ExpenseRed else IncomeGreen
                                val txAmountStr = "$sign${format.format(tx.amount)}"
                                val dateFormat = SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault())

                                ModernTransactionCard(
                                    icon = icon,
                                    title = tx.title.ifBlank { tx.category },
                                    category = tx.category,
                                    payment = tx.paymentMethod.ifBlank { "UPI" },
                                    date = dateFormat.format(Date(tx.date)),
                                    amount = txAmountStr,
                                    colorBg = colorBg,
                                    amountColor = colorText,
                                    onClick = { selectedTxForDetails = tx }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Transaction Detail Dialog
        selectedTxForDetails?.let { tx ->
            TransactionDetailDialog(
                transaction = tx,
                onDismiss = { selectedTxForDetails = null },
                onDelete = {
                    viewModel.deleteTransaction(it)
                    selectedTxForDetails = null
                }
            )
        }

        // Floating Frosted Bottom Bar
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            FrostedBottomBar(
                currentRoute = currentRoute,
                onNavigate = onNavigateBottomBar,
                onQuickAdd = onNavigateToAddTransaction
            )
        }
    }
}

@Composable
fun ModernTransactionCard(
    icon: String,
    title: String,
    category: String,
    payment: String,
    date: String,
    amount: String,
    colorBg: Color,
    amountColor: Color,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(),
        label = "tx_card_scale"
    )

    val cleanTitle = title.replace("\r", " ").replace("\n", " ").trim().replace(Regex("\\s+"), " ").ifBlank { category }
    val cleanCategory = category.replace("\r", " ").replace("\n", " ").trim().ifBlank { "" }
    val rawPayment = payment.replace("\r", " ").replace("\n", " ").trim().replace(Regex("\\s+"), " ").ifBlank { "UPI" }
    val cleanPayment = when {
        rawPayment.contains("Amazon Pay ICICI", ignoreCase = true) -> "Amazon Pay"
        rawPayment.length > 14 -> rawPayment.take(12) + "…"
        else -> rawPayment
    }
    val cleanDate = date.replace("\r", " ").replace("\n", " ").trim()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clip(RoundedCornerShape(16.dp))
            .background(SlateCardSurface)
            .border(1.dp, GlassCardBorder, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(colorBg.copy(alpha = 0.15f))
                    .border(1.dp, colorBg.copy(alpha = 0.3f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 19.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Center Column: Merchant Name / Title & Subtitle with Date
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cleanTitle,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                val subtitleText = if (cleanCategory.isNotBlank() && !cleanCategory.equals("other", true) && !cleanCategory.equals(cleanTitle, true)) {
                    "$cleanCategory • $cleanDate"
                } else {
                    cleanDate
                }
                Text(
                    text = subtitleText,
                    fontSize = 11.5.sp,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right Column: Amount & Payment Pill
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = amount,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SlateCardElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = cleanPayment,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
