package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.Transaction
import com.example.ui.components.FrostedBottomBar
import com.example.ui.components.GradientButton
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
    var hideBalance by remember { mutableStateOf(false) }

    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val balanceStr = if (hideBalance) "••••••" else format.format(balance)
    val incomeStr = format.format(income)
    val expenseStr = format.format(expense)

    val firstName = user?.fullName?.substringBefore(" ")?.takeIf { it.isNotBlank() } ?: "User"
    val initials = user?.fullName?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercase() }?.joinToString("")?.take(2)?.ifBlank { "U" } ?: "TU"

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
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal)
                            .clickable { onNavigateBottomBar("profile") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = MaterialTheme.colorScheme.surface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "$greeting, $firstName 👋",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = todayStr,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Total Balance Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0x1A000000))
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp)
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
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = balanceStr,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Toggle Balance Visibility",
                                tint = TextTertiary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { hideBalance = !hideBalance }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Income & Expense Breakdown Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Income Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Income", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            incomeStr,
                                            fontSize = 14.sp,
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
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text("Expense", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            expenseStr,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action CTAs (Add Transaction & Scan)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Primary Add Transaction Button
                            Button(
                                onClick = onNavigateToAddTransaction,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Add Entry", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }

                            // Scan Receipt Button
                            OutlinedButton(
                                onClick = onNavigateToScanner,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF3B82F6)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(text = "🧾", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Scan Bill", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                // Recent Transactions Header & Section
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "See All →",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            modifier = Modifier.clickable { onNavigateToSeeAll() }
                        )
                    }

                    if (transactions.isEmpty()) {
                        // Polished Empty State Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0x0D000000))
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.background),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = PrimaryTeal.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = "No Transactions Logged Yet",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Add your first expense or scan a receipt\nto start tracking.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onNavigateToAddTransaction,
                                    modifier = Modifier.height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                    shape = RoundedCornerShape(22.dp)
                                ) {
                                    Text(text = "+ Add First Transaction", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else {
                        // Transactions List (Latest 5 items)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    "food" -> CatFoodBg
                                    "transport" -> CatTransportBg
                                    "shopping" -> CatShoppingBg
                                    "bills" -> CatBillsBg
                                    "health" -> CatHealthBg
                                    "entertainment" -> CatEntertainmentBg
                                    "education" -> CatEducationBg
                                    else -> if (isExp) ExpenseRedBg else IncomeGreenBg
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

            Spacer(modifier = Modifier.height(32.dp))
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

        // Floating Bottom Bar (handled externally or modified FrostedBottomBar)
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
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colorBg),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center Column: Merchant Name / Title & Subtitle with Date
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = cleanTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val subtitleText = if (cleanCategory.isNotBlank() && !cleanCategory.equals("other", true) && !cleanCategory.equals(cleanTitle, true)) {
                    "$cleanCategory • $cleanDate"
                } else {
                    cleanDate
                }
                Text(
                    text = subtitleText,
                    fontSize = 12.sp,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right Column: Amount & Payment Pill
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = amount,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cleanPayment,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
