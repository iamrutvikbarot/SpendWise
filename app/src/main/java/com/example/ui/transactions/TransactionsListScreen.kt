package com.example.ui.transactions

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.Transaction
import com.example.ui.home.HomeViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsListScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayTimeFormat = SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault())
    val listState = rememberLazyListState()

    var selectedTxForDetails by remember { mutableStateOf<Transaction?>(null) }

    // Group transactions cleanly by Month and Year
    val groupedTransactions = remember(transactions) {
        transactions
            .sortedByDescending { it.date }
            .groupBy { monthYearFormat.format(Date(it.date)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ==========================================
            // CLEAN MINIMAL TOP APP BAR
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                val backInteraction = remember { MutableInteractionSource() }
                val isBackPressed by backInteraction.collectIsPressedAsState()
                val backScale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (isBackPressed) 0.90f else 1f,
                    label = "back_btn_scale"
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(backScale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .clickable(
                            interactionSource = backInteraction,
                            indication = null,
                            onClick = onNavigateBack
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "All Activity",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${transactions.size} transactions recorded",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp
                    )
                }
            }

            // ==========================================
            // TIMELINE LIST
            // ==========================================
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "📜", fontSize = 36.sp)
                        Text(
                            text = "No Activity Yet",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Your income and expense logs will appear here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedTransactions.forEach { (monthYear, monthTransactions) ->
                        val parts = monthYear.split(" ")
                        val monthName = parts.getOrNull(0) ?: monthYear
                        val yearName = parts.getOrNull(1) ?: ""

                        val monthTotalSpent = monthTransactions
                            .filter { it.type.equals("EXPENSE", ignoreCase = true) }
                            .sumOf { it.amount }

                        // ==========================================
                        // NEAT & CLEAN STICKY MONTH HEADER
                        // ==========================================
                        stickyHeader(key = "header_$monthYear") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Month and Year
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = monthName,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (yearName.isNotEmpty()) {
                                            Text(
                                                text = yearName,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Clean Total Spent Summary for the month
                                    if (monthTotalSpent > 0) {
                                        Text(
                                            text = "Spent ${currencyFormat.format(monthTotalSpent)}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        Text(
                                            text = "${monthTransactions.size} items",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // NEAT TRANSACTION ROW
                        // ==========================================
                        items(monthTransactions, key = { it.id }) { tx ->
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

                            val sign = if (isExp) "-" else "+"
                            val amountColor = if (isExp) ExpenseRed else IncomeGreen
                            val formattedAmount = "$sign${currencyFormat.format(tx.amount)}"
                            val formattedDate = dayTimeFormat.format(Date(tx.date))

                            val interactionSource = remember { MutableInteractionSource() }
                            val isPressed by interactionSource.collectIsPressedAsState()
                            val itemScale by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = if (isPressed) 0.98f else 1f,
                                label = "tx_press_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(itemScale)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        onClick = { selectedTxForDetails = tx }
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Left: Category Emoji & Title + Date
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Simple Circle Avatar with Emoji
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surface),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = icon, fontSize = 20.sp)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = tx.title.ifBlank { tx.category },
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontSize = 14.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = formattedDate,
                                                color = TextTertiary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Right: Amount
                                    Text(
                                        text = formattedAmount,
                                        color = amountColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transaction Detail Sheet when tapped
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
    }
}
