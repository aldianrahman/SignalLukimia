package com.lukimia.signalapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lukimia.signalapp.auth.AuthScreen
import com.lukimia.signalapp.chat.ChatScreen
import com.lukimia.signalapp.home.HomeScreen
import com.lukimia.signalapp.profile.ProfileScreen
import com.lukimia.signalapp.scan.ScanScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") { AuthScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("chat/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            ChatScreen(navController, chatId)
        }
        composable("scan") { ScanScreen(navController) }
    }
}