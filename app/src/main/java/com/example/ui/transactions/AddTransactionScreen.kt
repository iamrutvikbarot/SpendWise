package com.example.ui.transactions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassTextField
import com.example.ui.components.GradientButton
import com.example.ui.theme.*

data class CategoryItem(val name: String, val icon: String, val color: Color)

@Composable
fun AddTransactionScreen(
    viewModel: TransactionViewModel,
    onNavigateBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("Food") }
    var selectedPayment by remember { mutableStateOf("UPI") }

    val expenseCategories = remember {
        listOf(
            CategoryItem("Food", "🍔", FoodColor),
            CategoryItem("Transport", "🚕", TransportColor),
            CategoryItem("Shopping", "🛍️", ShoppingColor),
            CategoryItem("Bills", "📄", BillsColor),
            CategoryItem("Health", "⚕️", HealthColor),
            CategoryItem("Entertainment", "🍿", EntertainmentColor),
            CategoryItem("Education", "📚", EducationColor),
            CategoryItem("Groceries", "🛒", Color(0xFF10B981)),
            CategoryItem("Travel", "✈️", Color(0xFF38BDF8)),
            CategoryItem("Personal Care", "💅", Color(0xFFF472B6)),
            CategoryItem("Utilities", "⚡", Color(0xFFFBBF24)),
            CategoryItem("Others", "💸", OthersColor)
        )
    }

    val incomeCategories = remember {
        listOf(
            CategoryItem("Salary", "💼", Color(0xFF10B981)),
            CategoryItem("Freelance", "💻", Color(0xFF06B6D4)),
            CategoryItem("Business", "📈", Color(0xFF3B82F6)),
            CategoryItem("Investments", "📊", Color(0xFF8B5CF6)),
            CategoryItem("Bonus", "🎁", Color(0xFFEC4899)),
            CategoryItem("Dividends", "💰", Color(0xFFEAB308)),
            CategoryItem("Rental", "🏠", Color(0xFF14B8A6)),
            CategoryItem("Refund", "🔄", Color(0xFF6366F1)),
            CategoryItem("Cashback", "💳", Color(0xFF059669)),
            CategoryItem("Gift", "🎀", Color(0xFFF43F5E)),
            CategoryItem("Others", "💵", OthersColor)
        )
    }

    val activeCategories = if (isExpense) expenseCategories else incomeCategories

    val payments = listOf("UPI", "Debit Card", "Credit Card", "Cash", "Net Banking")

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SlateCardElevated)
                        .border(1.dp, GlassCardBorder, CircleShape)
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "New Transaction",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Segmented Type Selector (Money Out Expense vs Money In Income)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SlateCardSurface)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(18.dp))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Expense Tab (Money Out ↗)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isExpense) ExpenseRedBg else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isExpense) ExpenseRed.copy(alpha = 0.5f) else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    if (!isExpense) {
                                        isExpense = true
                                        selectedCategory = "Food"
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isExpense) ExpenseRed.copy(alpha = 0.25f) else SlateCardElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallMade, // Outgoing Money Out ↗
                                        contentDescription = "Expense Out",
                                        tint = if (isExpense) ExpenseRed else TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Expense",
                                        color = if (isExpense) ExpenseRed else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Money Out",
                                        color = if (isExpense) ExpenseRed.copy(alpha = 0.8f) else TextTertiary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Income Tab (Money In / Bank Deposit ↙)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (!isExpense) IncomeGreenBg else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (!isExpense) PrimaryEmerald.copy(alpha = 0.5f) else Color.Transparent,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    if (isExpense) {
                                        isExpense = false
                                        selectedCategory = "Salary"
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (!isExpense) IncomeGreen.copy(alpha = 0.25f) else SlateCardElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CallReceived, // Incoming Money In ↙
                                        contentDescription = "Income In",
                                        tint = if (!isExpense) IncomeGreen else TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Income",
                                        color = if (!isExpense) IncomeGreen else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Money In (Bank)",
                                        color = if (!isExpense) IncomeGreen.copy(alpha = 0.8f) else TextTertiary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount Hero Input Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SlateCardSurface)
                        .border(1.dp, GlassCardBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isExpense) "EXPENSE AMOUNT (OUT)" else "INCOME AMOUNT (IN TO BANK)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = if (isExpense) ExpenseRed.copy(alpha = 0.9f) else PrimaryEmerald.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "₹",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) ExpenseRed else IncomeGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TextField(
                                value = amount,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() || it == '.' }) {
                                        amount = input
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "0",
                                        color = TextTertiary,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                textStyle = LocalTextStyle.current.copy(
                                    color = TextPrimary,
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Start
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = if (isExpense) ExpenseRed else IncomeGreen
                                ),
                                modifier = Modifier.widthIn(min = 60.dp, max = 220.dp)
                            )
                        }
                    }
                }

                // Note / Title Field
                GlassTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = if (isExpense) "Title or Merchant Name" else "Income Source / Description",
                    placeholder = if (isExpense) "e.g. Swiggy, Uber, Indane Gas, Amazon" else "e.g. Monthly Salary, Freelance Client, Dividend"
                )

                // Category Selection with distinct Expense vs Income sets
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpense) "Expense Category" else "Income Category",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedCategory,
                            color = if (isExpense) ExpenseRed else PrimaryEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 4 columns grid for categories with AnimatedContent
                    AnimatedContent(
                        targetState = isExpense,
                        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                        label = "category_transition"
                    ) { expenseState ->
                        val currentList = if (expenseState) expenseCategories else incomeCategories
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentList.chunked(4).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { cat ->
                                        val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)
                                        val interactionSource = remember { MutableInteractionSource() }
                                        val isPressed by interactionSource.collectIsPressedAsState()
                                        val itemScale by animateFloatAsState(
                                            targetValue = if (isPressed) 0.93f else 1f,
                                            animationSpec = spring(),
                                            label = "cat_scale"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .scale(itemScale)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (isSelected) cat.color.copy(alpha = 0.22f) else SlateCardSurface)
                                                .border(
                                                    1.dp,
                                                    if (isSelected) cat.color else GlassCardBorder,
                                                    RoundedCornerShape(14.dp)
                                                )
                                                .clickable(
                                                    interactionSource = interactionSource,
                                                    indication = null,
                                                    onClick = { selectedCategory = cat.name }
                                                )
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(text = cat.icon, fontSize = 20.sp)
                                                Text(
                                                    text = cat.name,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) TextPrimary else TextSecondary,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Payment Method Selector
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isExpense) "Payment Method" else "Deposit Destination / Method",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(payments) { pay ->
                            val isSelected = selectedPayment == pay
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) PrimaryEmerald.copy(alpha = 0.2f) else SlateCardElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) PrimaryEmerald else GlassCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedPayment = pay }
                                    .padding(horizontal = 16.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pay,
                                    color = if (isSelected) PrimaryEmerald else TextSecondary,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Save Action
                val isValid = (amount.toDoubleOrNull() ?: 0.0) > 0
                GradientButton(
                    text = if (isExpense) "Save Expense Entry" else "Save Income Entry",
                    onClick = {
                        val amountVal = amount.toDoubleOrNull() ?: 0.0
                        val finalTitle = title.ifBlank { selectedCategory }
                        viewModel.addTransaction(
                            amount = amountVal,
                            title = finalTitle,
                            isExpense = isExpense,
                            category = selectedCategory,
                            paymentMethod = selectedPayment,
                            onComplete = onNavigateBack
                        )
                    },
                    enabled = isValid,
                    gradientColors = if (isExpense) listOf(ExpenseRed, Color(0xFFE11D48)) else listOf(PrimaryGradientStart, PrimaryGradientEnd)
                )
            }
        }
    }
}

