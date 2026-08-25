package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FrostedBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onQuickAdd: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft neon underglow behind dock
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(34.dp)
                .offset(y = 8.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrimaryEmerald.copy(alpha = 0.28f),
                            PrimaryTeal.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Floating Glass Dock Capsule
        Box(
            modifier = Modifier
                .width(220.dp)
                .height(60.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(30.dp),
                    spotColor = Color(0xFF000000),
                    ambientColor = PrimaryEmerald.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF0182234),
                            Color(0xFA0D1522)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x7034D399),
                            Color(0x20FFFFFF),
                            Color(0x05000000)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Nav Item (Fixed Slot)
                DockTabItem(
                    emoji3d = "🏠",
                    contentDescription = "Home",
                    isSelected = currentRoute == "home",
                    onClick = { onNavigate("home") }
                )

                // Central Elevated 3D Quick Add Button
                val addInteraction = remember { MutableInteractionSource() }
                val isAddPressed by addInteraction.collectIsPressedAsState()
                val addScale by animateFloatAsState(
                    targetValue = if (isAddPressed) 0.88f else 1f,
                    animationSpec = spring(),
                    label = "dock_add_scale"
                )

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .scale(addScale)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = PrimaryEmerald,
                            ambientColor = PrimaryEmerald
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF34D399),
                                    Color(0xFF059669),
                                    Color(0xFF047857)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xC0FFFFFF),
                                    Color(0x3034D399)
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable(
                            interactionSource = addInteraction,
                            indication = null,
                            onClick = onQuickAdd
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        tint = Color(0xFF041E16),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Profile Nav Item (Fixed Slot)
                DockTabItem(
                    emoji3d = "👤",
                    contentDescription = "Profile",
                    isSelected = currentRoute == "profile",
                    onClick = { onNavigate("profile") }
                )
            }
        }
    }
}

@Composable
private fun DockTabItem(
    emoji3d: String,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.18f else 0f,
        animationSpec = tween(200),
        label = "dock_tab_bg"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.55f,
        animationSpec = tween(200),
        label = "dock_tab_alpha"
    )

    val itemScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(),
        label = "dock_item_press"
    )

    Box(
        modifier = Modifier
            .width(54.dp)
            .height(44.dp)
            .scale(itemScale)
            .clip(RoundedCornerShape(22.dp))
            .background(PrimaryEmerald.copy(alpha = bgAlpha))
            .then(
                if (isSelected) {
                    Modifier.border(
                        1.dp,
                        PrimaryEmerald.copy(alpha = 0.35f),
                        RoundedCornerShape(22.dp)
                    )
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji3d,
                fontSize = 20.sp,
                modifier = Modifier.alpha(contentAlpha)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(PrimaryEmerald)
                )
            }
        }
    }
}


