package com.example.simplenote.navigation

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.simplenote.AppGraph
import com.example.simplenote.ui.screens.home.HomeEmptyScreen
import com.example.simplenote.ui.screens.home.HomeNotesScreen
import com.example.simplenote.ui.screens.home.HomeViewModel
import com.example.simplenote.ui.screens.login.LoginScreen
import com.example.simplenote.ui.screens.note.NoteEditorScreen
import com.example.simplenote.ui.screens.register.RegisterScreen
import com.example.simplenote.ui.screens.settings.ChangePasswordScreen
import com.example.simplenote.ui.screens.settings.SettingsScreen
import com.example.simplenote.ui.screens.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Splash
        composable("splash") {
            SplashScreen { isLoggedIn ->
                if (isLoggedIn) {
                    navController.navigate(Screen.Home.route) { popUpTo(0) }
                } else {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            }
        }

        // Login
        composable(Screen.Login.route) {
            LoginScreen(
                onLoggedIn = { navController.navigate(Screen.Home.route) { popUpTo(0) } },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        // Register
        composable(Screen.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegistered = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            val ctx = LocalContext.current
            val scope = rememberCoroutineScope()

            val vm: HomeViewModel =
                viewModel(factory = HomeViewModel.factory(ctx))
            val state by vm.state.collectAsState()

            // ✅ فقط وقتی هیچ نوتی نداریم (count == 0) EmptyScreen بیا
// فقط وقتی هیچ نوتی نداریم و جستجو هم خالی است، صفحهٔ خالی بیا
            val showEmpty = !state.isLoading && state.query.isBlank() && state.count == 0

            if (showEmpty) {
                HomeEmptyScreen(
                    onCreateNote = { navController.navigate("noteEditor") },
                    onClickHome = { /* همین صفحه */ },
                    onClickSettings = { navController.navigate("settings") }
                )
            } else {
                HomeNotesScreen(
                    onCreateNote = { navController.navigate("noteEditor") },
                    onOpenNote = { id ->
                        // ✅ اگر آفلاین و نوت در کش نیست → نرو
                        scope.launch {
                            val local = AppGraph.notesRepository.getLocalById(id)
                            if (local != null) {
                                navController.navigate("noteEditor?noteId=$id")
                            } else {
                                if (isOnline(ctx)) {
                                    // آنلاینیم: برو، VM خودش از سرور میاره و کش می‌کنه
                                    navController.navigate("noteEditor?noteId=$id")
                                } else {
                                    Toast.makeText(
                                        ctx,
                                        "این یادداشت در کش نیست و اینترنت هم ندارید.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    onClickSettings = { navController.navigate("settings") }
                )
            }
        }

        // Settings
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onChangePassword = { navController.navigate("changePassword") },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        // Change Password
        composable("changePassword") {
            ChangePasswordScreen(onBack = { navController.popBackStack() })
        }

        // Note Editor
        composable(
            route = "noteEditor?noteId={noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val noteIdStr = backStackEntry.arguments?.getString("noteId")
            val noteId = noteIdStr?.toIntOrNull()

            NoteEditorScreen(
                noteId = noteId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0); launchSingleTop = true
                    }
                },
                onDeleted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0); launchSingleTop = true
                    }
                }
            )
        }
    }
}

/** وضعیت اینترنت */
private fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
