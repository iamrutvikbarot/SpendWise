package com.example.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAnimationComplete: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SplashDarkTop,
                        SplashDarkBottom
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative wave-like shape at the bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, SplashWaveColor)
                    ),
                    shape = RoundedCornerShape(topStart = 200.dp, topEnd = 100.dp)
                )
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, SplashDarkBottom)
                    ),
                    shape = RoundedCornerShape(topStart = 100.dp, topEnd = 200.dp)
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Custom Ribbon Logo
            com.example.ui.components.SpendWiseLogo(modifier = Modifier.size(130.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Clean Brand Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Spend",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Wise",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryTeal,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Welcome to your financial vault",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.LightGray
            )
        }
    }
}
