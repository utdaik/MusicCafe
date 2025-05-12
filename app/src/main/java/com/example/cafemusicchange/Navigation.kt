package com.example.cafemusicchange

import PlayScreen
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cafemusicchange.ui.home.HomeScreen
import com.example.cafemusicchange.ui.library.FavoriteScreen
import com.example.cafemusicchange.ui.login.SignInScreen
import com.example.cafemusicchange.ui.login.SignUpScreen
import com.example.cafemusicchange.ui.login.WelcomeScreen
import com.example.cafemusicchange.ui.playlist.PlaylistDetailScreen
import com.example.cafemusicchange.ui.playlist.PlaylistScreen
import com.example.cafemusicchange.ui.search.SearchScreen
import com.example.cafemusicchange.ui.settings.SettingScreen

import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Welcome : Screen("welcome")
    object SignUp : Screen("signup")
    object SingIn : Screen("signin")
    object Search : Screen("search")
    object Player : Screen("player")
    object Settings : Screen("settings")
    object Playlist : Screen("playlist")
    object PlaylistDetail : Screen("playlist_detail")
    object Favorite : Screen("favorite")
    object Download : Screen("download")
    object History : Screen("history")
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val mainState = mainViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var userId = mainState.value.userId
    // Lấy route hiện tại thông qua currentBackStackEntryAsState
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: ""

    LaunchedEffect(Unit) {
        snapshotFlow { mainState.value.Error }
            .collectLatest { errorMessage ->
                if (errorMessage.isNotEmpty()) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    mainViewModel.setError("") // Reset lỗi sau khi hiển thị
                }
            }
    }


    LaunchedEffect(Unit) {
        snapshotFlow { mainState.value.Notified }
            .collectLatest { message ->
                if (message.isNotEmpty()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    mainViewModel.setNotified("") // Reset thông báo sau khi hiển thị
                }
            }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        // Bỏ hết inset mặc định (statusBar, navBar)…
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Ẩn bottom bar khi route bắt đầu bằng "player"
            if (userId==null){
                userId=100000
            }
            val hiddenScreens = listOf("player", "welcome", "signin","signup","settings")
            if (!hiddenScreens.any { currentRoute.startsWith(it) }) {
                BottomNavigationBar(navController, userId!!)
            }
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Welcome.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                composable(Screen.Welcome.route) {
                    WelcomeScreen(navController)
                }
                composable(Screen.SignUp.route) {
                    SignUpScreen(navController, hiltViewModel(), mainViewModel)
                }
                composable(Screen.SingIn.route) {
                    SignInScreen(navController, hiltViewModel(), mainViewModel)
                }
                composable(Screen.Home.route) {
                    HomeScreen(navController, hiltViewModel(), mainViewModel)
                }
                composable("player/{tracksJson}/{startIndex}") { backStackEntry ->
                    val tracksJson = backStackEntry.arguments?.getString("tracksJson") ?: "[]"
                    val trackIndex = backStackEntry.arguments?.getString("startIndex")?.toIntOrNull() ?: 0
                    PlayScreen(tracksJson, trackIndex, hiltViewModel(), navController,mainViewModel)
                }


                composable(Screen.Search.route) {
                    SearchScreen(navController, hiltViewModel(), mainViewModel)
                }
                composable("playlist/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: 0L
                    PlaylistScreen(navController, hiltViewModel(), userId)
                }

                composable("playlist_detail/{playlistId}/{userId}") { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0L
                    val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: 0L
                    PlaylistDetailScreen(navController, hiltViewModel(), playlistId, userId)
                }

                composable(Screen.Settings.route) {
                    SettingScreen()
                }

                composable("favorite/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: 0L
                    FavoriteScreen(navController, hiltViewModel(), userId)
                }

            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController, userId: Long) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Home.route) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Search.route) },
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            label = { Text("Search") }
        )
        NavigationBarItem(
            selected = false,
            onClick = {
                Log.i("Check userd id in nav" ,userId.toString())
                navController.navigate("playlist/$userId") },
            icon = { Icon(Icons.Filled.LibraryMusic, contentDescription = "Library") },
            label = { Text("Library") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Screen.Settings.route) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") }
        )
    }
}
