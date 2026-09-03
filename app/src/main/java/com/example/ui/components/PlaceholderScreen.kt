package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PlaceholderScreen(
    title: String,
    currentRoute: String,
    onNavigateBottomBar: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Background, Background, Background)))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
            Text(title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            FrostedBottomBar(currentRoute = currentRoute, onNavigate = onNavigateBottomBar)
        }
    }
}
