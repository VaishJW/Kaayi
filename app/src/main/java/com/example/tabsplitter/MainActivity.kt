package com.example.tabsplitter

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Person
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tabsplitter.data.database.AppDatabase
import com.example.tabsplitter.ui.AddFriendScreen
import com.example.tabsplitter.ui.HomeScreen
import com.example.tabsplitter.ui.TabDetailScreen
import com.example.tabsplitter.ui.CategoriesScreen
import com.example.tabsplitter.ui.SplitBillScreen
import com.example.tabsplitter.ui.SplitBillsListScreen
import com.example.tabsplitter.ui.SplitBillDetailScreen
import com.example.tabsplitter.ui.ProfileScreen
import com.example.tabsplitter.ui.TabSplitterViewModel
import com.example.tabsplitter.ui.TabSplitterViewModelFactory
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.CallSplit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.Dp

import com.example.tabsplitter.ui.theme.TabSplitterTheme

class MainActivity : ComponentActivity() {
    private val _navEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val navEvent = _navEvent.asSharedFlow()
    private var pendingRoute: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        handleIntent(intent)

        setContent {
            TabSplitterTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                
                // Request Permission launcher for Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }
                
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Handle routing events (from notification click)
                LaunchedEffect(navController) {
                    pendingRoute?.let { route ->
                        navController.navigate(route) {
                            popUpTo("home") { inclusive = false }
                        }
                        pendingRoute = null
                    }
                    
                    navEvent.collect { route ->
                        navController.navigate(route) {
                            popUpTo("home") { inclusive = false }
                        }
                    }
                }
                
                // Initialize database and DAOs
                val database = AppDatabase.getInstance(applicationContext)
                val friendDao = database.friendDao()
                val tabDao = database.tabDao()
                val transactionDao = database.transactionDao()
                val categoryDao = database.categoryDao()
                val splitBillDao = database.splitBillDao()
                
                // Get ViewModel using factory
                val viewModel: TabSplitterViewModel = viewModel(
                    factory = TabSplitterViewModelFactory(friendDao, tabDao, transactionDao, categoryDao, splitBillDao)
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        AnimatedVisibility(
                            visible = currentRoute == "home" || currentRoute == "categories_breakdown" || currentRoute == "split_bills_list" || currentRoute == "profile",
                            enter = fadeIn(tween(200)) + slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(200)
                            ),
                            exit = fadeOut(tween(200)) + slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = tween(200)
                            )
                        ) {
                            NavigationBar(
                                modifier = Modifier
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp)),
                                containerColor = Color.Black.copy(alpha = 0.6f),
                                tonalElevation = 0.dp,
                                contentColor = Color.White
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        if (currentRoute != "home") {
                                            navController.navigate("home") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.Outlined.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        selectedTextColor = Color.White,
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                        indicatorColor = Color.Transparent
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "categories_breakdown",
                                    onClick = {
                                        if (currentRoute != "categories_breakdown") {
                                            navController.navigate("categories_breakdown") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.Outlined.BarChart, contentDescription = "Categories") },
                                    label = { Text("Categories") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        selectedTextColor = Color.White,
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                        indicatorColor = Color.Transparent
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "split_bills_list",
                                    onClick = {
                                        if (currentRoute != "split_bills_list") {
                                            navController.navigate("split_bills_list") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.AutoMirrored.Filled.CallSplit, contentDescription = "Split Bill") },
                                    label = { Text("Split Bill") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        selectedTextColor = Color.White,
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                        indicatorColor = Color.Transparent
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        if (currentRoute != "profile") {
                                            navController.navigate("profile") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        unselectedIconColor = Color.White.copy(alpha = 0.5f),
                                        selectedTextColor = Color.White,
                                        unselectedTextColor = Color.White.copy(alpha = 0.5f),
                                        indicatorColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onAddFriendClick = { navController.navigate("add_friend") },
                                onSplitBillClick = { navController.navigate("split_bill") },
                                onTabClick = { tabId, friendName ->
                                    navController.navigate("tab_detail/$tabId/$friendName")
                                },
                                bottomBarPadding = innerPadding.calculateBottomPadding()
                            )
                        }
                        composable("categories_breakdown") {
                            CategoriesScreen(
                                viewModel = viewModel,
                                bottomBarPadding = innerPadding.calculateBottomPadding()
                            )
                        }
                        composable("split_bills_list") {
                            SplitBillsListScreen(
                                viewModel = viewModel,
                                onCreateSplitBillClick = { navController.navigate("split_bill") },
                                onSplitBillClick = { splitBillId ->
                                    navController.navigate("split_bill_detail/$splitBillId")
                                },
                                bottomBarPadding = innerPadding.calculateBottomPadding()
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                viewModel = viewModel,
                                bottomBarPadding = innerPadding.calculateBottomPadding()
                            )
                        }
                        composable("add_friend") {
                            AddFriendScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("split_bill") {
                            SplitBillScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "split_bill_detail/{splitBillId}",
                            arguments = listOf(
                                navArgument("splitBillId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val splitBillId = backStackEntry.arguments?.getLong("splitBillId") ?: -1L
                            SplitBillDetailScreen(
                                viewModel = viewModel,
                                splitBillId = splitBillId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "tab_detail/{tabId}/{friendName}",
                            arguments = listOf(
                                navArgument("tabId") { type = NavType.LongType },
                                navArgument("friendName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val tabId = backStackEntry.arguments?.getLong("tabId") ?: -1L
                            val friendName = backStackEntry.arguments?.getString("friendName") ?: ""
                            TabDetailScreen(
                                viewModel = viewModel,
                                tabId = tabId,
                                friendName = friendName,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val tabId = intent.getLongExtra("tabId", -1L)
        val friendName = intent.getStringExtra("friendName")
        if (tabId != -1L && !friendName.isNullOrEmpty()) {
            val route = "tab_detail/$tabId/$friendName"
            pendingRoute = route
            _navEvent.tryEmit(route)
            // Clear to avoid re-triggering
            intent.removeExtra("tabId")
            intent.removeExtra("friendName")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "New Tabs"
            val descriptionText = "Notifications for new tabs created with friends"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("new_tabs", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

