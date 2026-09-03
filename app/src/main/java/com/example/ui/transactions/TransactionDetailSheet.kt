package com.example.ui.transactions

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.Transaction
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionDetailDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onDelete: (Transaction) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isExpense = transaction.type.equals("EXPENSE", ignoreCase = true)
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val sign = if (isExpense) "-" else "+"
    val amountFormatted = "$sign${format.format(transaction.amount)}"
    val fullDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    val catLower = transaction.category.lowercase()
    val icon = when {
        catLower.contains("food") -> "🍔"
        catLower.contains("transport") -> "🚕"
        catLower.contains("shopping") -> "🛍️"
        catLower.contains("bill") || catLower.contains("gas") || catLower.contains("electric") -> "📄"
        catLower.contains("health") || catLower.contains("medical") -> "⚕️"
        catLower.contains("entertainment") || catLower.contains("movie") -> "🍿"
        catLower.contains("education") -> "📚"
        catLower.contains("salary") -> "💼"
        catLower.contains("freelance") -> "💻"
        catLower.contains("investment") -> "📊"
        catLower.contains("bonus") -> "🎁"
        catLower.contains("dividend") -> "💰"
        catLower.contains("rental") -> "🏠"
        catLower.contains("refund") -> "🔄"
        catLower.contains("cashback") -> "💳"
        else -> if (isExpense) "💸" else "💵"
    }

    val catColor = when {
        catLower.contains("food") -> CatFood
        catLower.contains("transport") -> CatTransport
        catLower.contains("shopping") -> CatShopping
        catLower.contains("bill") || catLower.contains("gas") -> CatBills
        catLower.contains("health") -> CatHealth
        catLower.contains("entertainment") -> CatEntertainment
        catLower.contains("education") -> CatEducation
        !isExpense -> IncomeGreen
        else -> CatOthers
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            if (isExpense) ExpenseRed.copy(alpha = 0.5f) else PrimaryTeal.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.outline
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with Title & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction Details",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Hero Amount Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isExpense) ExpenseRed.copy(alpha = 0.1f) else IncomeGreen.copy(alpha = 0.1f))
                        .border(
                            1.dp,
                            if (isExpense) ExpenseRed.copy(alpha = 0.2f) else IncomeGreen.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Directional Badge (Money In vs Money Out)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isExpense) ExpenseRed.copy(alpha = 0.2f) else IncomeGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpense) Icons.Default.CallMade else Icons.Default.CallReceived,
                                contentDescription = null,
                                tint = if (isExpense) ExpenseRed else IncomeGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isExpense) "Money Out • Expense" else "Money In • Bank Credit",
                                color = if (isExpense) ExpenseRed else IncomeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = amountFormatted,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isExpense) ExpenseRed else IncomeGreen
                        )

                        Text(
                            text = transaction.title.ifBlank { transaction.category },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Detailed Specifications Table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Row
                    DetailRow(
                        label = "Category",
                        value = "${transaction.category} $icon",
                        valueColor = catColor
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.8.dp)

                    // Payment Method Row
                    DetailRow(
                        label = "Payment Method",
                        value = transaction.paymentMethod.ifBlank { "UPI" },
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.8.dp)

                    // Date Row
                    DetailRow(
                        label = "Date",
                        value = fullDateFormat.format(Date(transaction.date)),
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )

                    Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.8.dp)

                    // Time Row
                    DetailRow(
                        label = "Time",
                        value = timeFormat.format(Date(transaction.date)),
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Reference ID or Tx ID
                    if (!transaction.upiRefId.isNullOrBlank()) {
                        Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.8.dp)
                        DetailRow(
                            label = "Ref / Tx ID",
                            value = transaction.upiRefId,
                            valueColor = TextTertiary
                        )
                    } else {
                        Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.8.dp)
                        DetailRow(
                            label = "Record ID",
                            value = "#TXN-${transaction.id.toString().padStart(5, '0')}",
                            valueColor = TextTertiary
                        )
                    }

                    // Optional Note
                    if (!transaction.note.isNullOrBlank()) {
                        Divider(color = MaterialTheme.colorScheme.outline, thickness = 0.8.dp)
                        DetailRow(
                            label = "Note",
                            value = transaction.note,
                            valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Delete Confirmation or Delete Button
                if (showDeleteConfirm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showDeleteConfirm = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                onDelete(transaction)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1.2f).height(44.dp)
                        ) {
                            Text("Confirm Delete", color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRedBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = ExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete", color = ExpenseRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        ) {
                            Text("Close", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.4f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
