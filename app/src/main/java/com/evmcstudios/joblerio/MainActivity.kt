package com.evmcstudios.joblerio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.evmcstudios.joblerio.data.Analytics
import com.evmcstudios.joblerio.data.AdManager
import com.evmcstudios.joblerio.data.PostbackManager
import com.evmcstudios.joblerio.data.ReEngagementManager
import com.evmcstudios.joblerio.data.ReferrerManager
import com.evmcstudios.joblerio.data.RemoteConfigManager
import com.evmcstudios.joblerio.data.UserPrefs
import com.evmcstudios.joblerio.screens.LoginScreen
import com.evmcstudios.joblerio.screens.MainScreen
import com.evmcstudios.joblerio.screens.ResumeEditorScreen
import com.evmcstudios.joblerio.screens.ResumeListScreen
import com.evmcstudios.joblerio.screens.ResumePreviewScreen
import com.evmcstudios.joblerio.screens.SimpleWebViewScreen
import com.evmcstudios.joblerio.screens.SplashScreen
import com.evmcstudios.joblerio.screens.ViewedJobsScreen
import com.evmcstudios.joblerio.screens.WebViewScreen
import com.evmcstudios.joblerio.screens.rememberHomeScreenState
import com.evmcstudios.joblerio.ui.theme.JoblerioTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageInfo = try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (_: Exception) { null }
        val currentVersion = packageInfo?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode.toInt() else @Suppress("DEPRECATION") it.versionCode
        } ?: 1
        val lastUpdateTime = packageInfo?.lastUpdateTime ?: 0L
        val firstInstallTime = packageInfo?.firstInstallTime ?: 0L

        Log.d("MainActivity", "Package info: version=$currentVersion, lastUpdate=$lastUpdateTime, firstInstall=$firstInstallTime")

        val clearedVersion = UserPrefs.checkAndClearOnVersionUpgrade(this, currentVersion)
        val clearedReinstall = UserPrefs.checkAndClearOnReinstall(this, lastUpdateTime, firstInstallTime)
        if (clearedVersion || clearedReinstall) {
            Log.d("MainActivity", "Stale data cleared: version=$clearedVersion, reinstall=$clearedReinstall")
            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                GoogleSignIn.getClient(this, gso).signOut()
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                Log.e("MainActivity", "Sign-out failed: ${e.message}")
            }
        }

        Analytics.init()
        AdManager.initialize(this)
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.scheduleReEngagementCheck(this)
        ReEngagementManager.updateLastActive(this)

        lifecycleScope.launch {
            try {
                RemoteConfigManager.init()
                Log.d("MainActivity", "Remote config initialized")
            } catch (e: Exception) {
                Log.e("MainActivity", "Remote config init failed: ${e.message}")
            }
            try {
                ReferrerManager.captureReferrer(this@MainActivity)
                PostbackManager.checkAndFirePostback(this@MainActivity)
                Log.d("MainActivity", "Referrer captured and postback checked")
            } catch (e: Exception) {
                Log.e("MainActivity", "Referrer/postback failed: ${e.message}")
            }
        }

        setContent {
            JoblerioTheme {
                JoblerioApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ReEngagementManager.updateLastActive(this)
    }
}

@Composable
fun JoblerioApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val userName = UserPrefs.getUserName(context)
    val startDest = if (UserPrefs.isLoggedIn(context)) "main" else "splash"
    val homeScreenState = rememberHomeScreenState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d("MainActivity", "Notification permission result: granted=$granted")
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    NavHost(navController = navController, startDestination = startDest) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("Navigation", "Navigated to: ${destination.route}")
        }
        composable("splash") {
            Log.d("Navigation", "Screen: splash")
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
            Log.d("Navigation", "Screen: login")
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
            Log.d("Navigation", "Screen: main")
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
                onOpenLink = { url, title ->
                    val encodedUrl = URLEncoder.encode(url, "UTF-8")
                    val encodedTitle = URLEncoder.encode(title, "UTF-8")
                    navController.navigate("simple_webview/$encodedUrl/$encodedTitle")
                },
                onResumeEdit = { resumeId ->
                    navController.navigate("resume_editor/$resumeId")
                },
                onViewedJobs = {
                    navController.navigate("viewed_jobs")
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
            Log.d("Navigation", "Screen: webview")
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
        composable(
            route = "simple_webview/{url}/{title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            Log.d("Navigation", "Screen: simple_webview")
            val url = URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8")
            val title = URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
            SimpleWebViewScreen(
                url = url,
                title = title,
                onBack = { navController.popBackStack() }
            )
        }
        composable("resume_list") {
            Log.d("Navigation", "Screen: resume_list")
            ResumeListScreen(
                onResumeClick = { resumeId ->
                    navController.navigate("resume_editor/$resumeId")
                },
                onCreateResume = {
                    navController.navigate("resume_editor/new")
                }
            )
        }
        composable(
            route = "resume_editor/{resumeId}",
            arguments = listOf(
                navArgument("resumeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            Log.d("Navigation", "Screen: resume_editor")
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: "new"
            ResumeEditorScreen(
                resumeId = if (resumeId == "new") null else resumeId,
                onBack = { navController.popBackStack() },
                onPreview = { id ->
                    navController.navigate("resume_preview/$id")
                }
            )
        }
        composable(
            route = "resume_preview/{resumeId}",
            arguments = listOf(
                navArgument("resumeId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            Log.d("Navigation", "Screen: resume_preview")
            val resumeId = backStackEntry.arguments?.getString("resumeId") ?: ""
            ResumePreviewScreen(
                resumeId = resumeId,
                onBack = { navController.popBackStack() }
            )
        }
        composable("viewed_jobs") {
            Log.d("Navigation", "Screen: viewed_jobs")
            ViewedJobsScreen(
                onBack = { navController.popBackStack() },
                onJobClick = { url, title, company, city, state, date, snippet ->
                    val args = listOf(url, title, company, city, state, date, snippet)
                        .joinToString("&") { URLEncoder.encode(it, "UTF-8") }
                    navController.navigate("webview/$args")
                }
            )
        }
    }
}
