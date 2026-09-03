package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.repository.UserRepository
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.LoginScreen
import com.example.ui.components.FrostedBottomBar
import com.example.ui.home.HomeDashboard
import com.example.ui.home.HomeViewModel
import com.example.ui.profile.ProfileScreen
import com.example.ui.scanner.ScannerScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.SpendWiseTheme
import com.example.ui.transactions.AddTransactionScreen
import com.example.ui.transactions.TransactionViewModel
import com.example.ui.transactions.TransactionsListScreen
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val _sharedImageUri = kotlinx.coroutines.flow.MutableStateFlow<android.net.Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        val database = AppDatabase.getDatabase(this)
        val userRepository = UserRepository(database.userDao())
        val transactionRepository = com.example.data.repository.TransactionRepository(database.transactionDao())
        val budgetRepository = com.example.data.repository.BudgetRepository(database.budgetDao())
        val dataStoreManager = DataStoreManager(this)
        val driveBackupManager = com.example.data.remote.DriveBackupManager(this, transactionRepository, budgetRepository)

        // If the app was closed and restarted, wipe guest session if any
        lifecycleScope.launch {
            val userId = dataStoreManager.userId.firstOrNull()
            if (userId != null) {
                val user = userRepository.getUserByIdFlow(userId).firstOrNull()
                if (user?.email == "guest@spendwise.app") {
                    dataStoreManager.clearLoginState()
                    transactionRepository.deleteTransactionsForUser(userId)
                }
            }
        }

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(userRepository, transactionRepository, dataStoreManager, driveBackupManager) as T
                    modelClass.isAssignableFrom(HomeViewModel::class.java) -> HomeViewModel(userRepository, transactionRepository, dataStoreManager) as T
                    modelClass.isAssignableFrom(TransactionViewModel::class.java) -> TransactionViewModel(transactionRepository, dataStoreManager, driveBackupManager) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }

        setContent {
            val themeMode by dataStoreManager.themeMode.collectAsState(initial = "system")
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            SpendWiseTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val isLoggedIn by dataStoreManager.isLoggedIn.collectAsState(initial = null)
                val sharedImageUri by _sharedImageUri.collectAsState()
                var showSplash by remember { mutableStateOf(true) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                LaunchedEffect(sharedImageUri, isLoggedIn, showSplash) {
                    if (sharedImageUri != null && isLoggedIn == true && !showSplash) {
                        navController.navigate("scanner")
                    }
                }

                Crossfade(
                    targetState = showSplash || isLoggedIn == null,
                    animationSpec = tween(400),
                    label = "splash_crossfade"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            isDarkTheme = darkTheme,
                            onAnimationComplete = {
                                showSplash = false
                            }
                        )
                    } else {
                        val startDestination = remember { if (isLoggedIn == true) "home" else "login" }
                        Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                            NavHost(
                                navController = navController,
                                startDestination = startDestination
                            ) {
                            composable(
                                route = "login",
                                enterTransition = { fadeIn(tween(250)) },
                                exitTransition = { fadeOut(tween(200)) }
                            ) {
                                val authViewModel: AuthViewModel = viewModel(factory = factory)
                                LoginScreen(
                                    viewModel = authViewModel,
                                    onLoginSuccess = {
                                        navController.navigate("home") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // Main Flow with Persistent Stationary Dock
                            composable(
                                route = "home",
                                enterTransition = { fadeIn(tween(250)) },
                                exitTransition = { fadeOut(tween(250)) }
                            ) {
                                MainTabScaffold(
                                    factory = factory,
                                    dataStoreManager = dataStoreManager,
                                    onNavigateToAddTransaction = { navController.navigate("add_transaction") },
                                    onNavigateToScanner = { navController.navigate("scanner") },
                                    onNavigateToSeeAll = { navController.navigate("transactions_list") },
                                    onLogout = {
                                        lifecycleScope.launch {
                                            dataStoreManager.clearLoginState()
                                            
                                            // Sign out from Google Auth so account picker shows next time
                                            val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                                            val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this@MainActivity, gso)
                                            googleSignInClient.signOut()
                                            
                                            navController.navigate("login") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }

                            composable(
                                route = "transactions_list",
                                enterTransition = {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) + fadeIn(tween(200))
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) + fadeOut(tween(200))
                                }
                            ) {
                                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                                TransactionsListScreen(
                                    viewModel = homeViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = "scanner",
                                enterTransition = {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) + fadeIn(tween(200))
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) + fadeOut(tween(200))
                                }
                            ) {
                                val txViewModel: TransactionViewModel = viewModel(factory = factory)
                                ScannerScreen(
                                    viewModel = txViewModel,
                                    initialUri = sharedImageUri,
                                    onNavigateBack = { 
                                        _sharedImageUri.value = null
                                        navController.popBackStack() 
                                    }
                                )
                            }

                            composable(
                                route = "add_transaction",
                                enterTransition = {
                                    slideIntoContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) + fadeIn(tween(200))
                                },
                                exitTransition = {
                                    slideOutOfContainer(
                                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                                    ) + fadeOut(tween(200))
                                }
                            ) {
                                val txViewModel: TransactionViewModel = viewModel(factory = factory)
                                AddTransactionScreen(
                                    viewModel = txViewModel,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        if (intent?.action == android.content.Intent.ACTION_SEND && intent.type?.startsWith("image/") == true) {
            val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(android.content.Intent.EXTRA_STREAM, android.net.Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
            }
            if (uri != null) {
                _sharedImageUri.value = uri
            }
        }
    }
}

@Composable
fun MainTabScaffold(
    factory: ViewModelProvider.Factory,
    dataStoreManager: DataStoreManager,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToSeeAll: () -> Unit,
    onLogout: () -> Unit
) {
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    var selectedTab by remember { mutableStateOf("home") }
    val userPhotoUrl by dataStoreManager.userPhotoUrl.collectAsState(initial = null)

    Box(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        // Content Area with smooth slide transition
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                val isGoingRight = targetState == "profile"
                slideInHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    initialOffsetX = { fullWidth -> if (isGoingRight) fullWidth else -fullWidth }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { fullWidth -> if (isGoingRight) -fullWidth else fullWidth }
                )
            },
            label = "tab_content_transition"
        ) { targetTab ->
            when (targetTab) {
                "home" -> {
                    HomeDashboard(
                        viewModel = homeViewModel,
                        currentRoute = "home",
                        onNavigateBottomBar = { selectedTab = it },
                        onNavigateToAddTransaction = onNavigateToAddTransaction,
                        onNavigateToScanner = onNavigateToScanner,
                        onNavigateToSeeAll = onNavigateToSeeAll
                    )
                }
                "profile" -> {
                    ProfileScreen(
                        viewModel = homeViewModel,
                        currentRoute = "profile",
                        onNavigateBottomBar = { selectedTab = it },
                        onNavigateToAddTransaction = onNavigateToAddTransaction,
                        onLogout = onLogout
                    )
                }
            }
        }

        // Stationary Floating Bottom Bar (Anchor-locked at bottom, never slides or jerks)
        Box(
            modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        ) {
            FrostedBottomBar(
                currentRoute = selectedTab,
                onNavigate = { selectedTab = it },
                onQuickAdd = onNavigateToAddTransaction,
                userPhotoUrl = userPhotoUrl
            )
        }
    }
}
