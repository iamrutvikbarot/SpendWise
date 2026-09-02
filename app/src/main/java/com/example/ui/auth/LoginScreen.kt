package com.example.ui.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.ui.components.GradientButton
import com.example.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                viewModel.loginWithGoogle(account)
            }
        } catch (e: ApiException) {
            viewModel.setError("Google sign in failed")
        }
    }

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
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFF000000))
                .clip(RoundedCornerShape(28.dp))
                .background(SlateCardSurface)
                .border(1.dp, GlassCardBorder, RoundedCornerShape(28.dp))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Brand Logo Emblem
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F261F), Color(0xFF103A2E), Color(0xFF0D1B2A))
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(PrimaryEmerald, PrimaryTeal)),
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = "SpendWise",
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Sparkle Accent
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFDE047), Color(0xFFEAB308)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Spend",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Wise",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryEmerald
                )
            }
            Text(
                text = "Welcome to your financial vault",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = ExpenseRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            GradientButton(
                text = if (authState is AuthState.Loading) "Authenticating..." else "Sign in with Google",
                onClick = {
                    val gsoBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestProfile()
                        .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
                        
                    // Only request ID token if we have a valid non-placeholder client ID
                    if (BuildConfig.GOOGLE_CLIENT_ID != "your_google_client_id_here") {
                        gsoBuilder.requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
                    }

                    val googleSignInClient = GoogleSignIn.getClient(context, gsoBuilder.build())
                    launcher.launch(googleSignInClient.signInIntent)
                },
                enabled = authState !is AuthState.Loading
            )
        }
    }
}
