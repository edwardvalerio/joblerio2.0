package com.evmcstudios.joblerio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.evmcstudios.joblerio.screens.LoginScreen
import com.evmcstudios.joblerio.screens.MainScreen
import com.evmcstudios.joblerio.screens.SplashScreen
import com.evmcstudios.joblerio.screens.WebViewScreen
import com.evmcstudios.joblerio.ui.theme.JoblerioTheme
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JoblerioTheme {
                JoblerioApp()
            }
        }
    }
}

@Composable
fun JoblerioApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen(
                onJobClick = { url, jobTitle, company, city, state, date, snippet ->
                    val args = listOf(url, jobTitle, company, city, state, date, snippet)
                        .joinToString("&") { URLEncoder.encode(it, "UTF-8") }
                    navController.navigate("webview/$args")
                }
            )
        }
        composable(
            route = "webview/{args}",
            arguments = listOf(
                navArgument("args") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val decoded = URLDecoder.decode(backStackEntry.arguments?.getString("args") ?: "", "UTF-8")
            val parts = decoded.split("&")
            val url = parts.getOrElse(0) { "" }
            val title = parts.getOrElse(1) { "" }
            val company = parts.getOrElse(2) { "" }
            val city = parts.getOrElse(3) { "" }
            val state = parts.getOrElse(4) { "" }
            val date = parts.getOrElse(5) { "" }
            val snippet = parts.getOrElse(6) { "" }

            WebViewScreen(
                url = url,
                title = title,
                jobTitle = title,
                jobCompany = company,
                jobCity = city,
                jobState = state,
                jobDate = date,
                jobSnippet = snippet,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
