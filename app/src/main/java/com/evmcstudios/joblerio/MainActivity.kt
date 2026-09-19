package com.evmcstudios.joblerio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.evmcstudios.joblerio.data.Analytics
import com.evmcstudios.joblerio.data.UserPrefs
import com.evmcstudios.joblerio.screens.LoginScreen
import com.evmcstudios.joblerio.screens.MainScreen
import com.evmcstudios.joblerio.screens.SplashScreen
import com.evmcstudios.joblerio.screens.WebViewScreen
import com.evmcstudios.joblerio.screens.rememberHomeScreenState
import com.evmcstudios.joblerio.ui.theme.JoblerioTheme
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Analytics.init()
        setContent {
            JoblerioTheme {
                JoblerioApp()
            }
        }
    }
}

@Composable
fun JoblerioApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val userName = UserPrefs.getUserName(context)
    val startDest = if (UserPrefs.isLoggedIn(context)) "main" else "splash"
    val homeScreenState = rememberHomeScreenState()

    NavHost(navController = navController, startDestination = startDest) {
        composable("splash") {
            LaunchedEffect(Unit) { Analytics.trackScreenView("Splash") }
            SplashScreen(
                onSplashFinished = {
                    if (UserPrefs.isLoggedIn(context)) {
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("login") {
            LaunchedEffect(Unit) { Analytics.trackScreenView("Login") }
            LoginScreen(
                onLoginSuccess = { name ->
                    Analytics.trackLogin("email")
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSkip = {
                    Analytics.trackSkipLogin()
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            LaunchedEffect(Unit) { Analytics.trackScreenView("Main") }
            val name = UserPrefs.getUserName(context)
            MainScreen(
                userName = name,
                homeScreenState = homeScreenState,
                onJobClick = { url, jobTitle, company, city, state, date, snippet ->
                    Analytics.trackJobClick(jobTitle, company)
                    val args = listOf(url, jobTitle, company, city, state, date, snippet)
                        .joinToString("&") { URLEncoder.encode(it, "UTF-8") }
                    navController.navigate("webview/$args")
                },
                onLogout = {
                    Analytics.trackLogout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "webview/{args}",
            arguments = listOf(
                navArgument("args") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            LaunchedEffect(Unit) { Analytics.trackScreenView("Job Detail") }
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
