package com.evmcstudios.joblerio.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.evmcstudios.joblerio.R
import com.evmcstudios.joblerio.data.Analytics
import com.evmcstudios.joblerio.data.PostbackManager
import com.evmcstudios.joblerio.data.RecentSearchManager
import com.evmcstudios.joblerio.data.ReferrerManager
import com.evmcstudios.joblerio.data.SavedJobsManager
import com.evmcstudios.joblerio.data.UserPrefs
import com.evmcstudios.joblerio.ui.theme.BackgroundWhite
import com.evmcstudios.joblerio.ui.theme.CardWhite
import com.evmcstudios.joblerio.ui.theme.PrimaryBlue
import com.evmcstudios.joblerio.ui.theme.TextGray
import com.evmcstudios.joblerio.ui.theme.TitleDark
import kotlinx.coroutines.launch

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String = "",
    homeScreenState: HomeScreenState = rememberHomeScreenState(),
    onJobClick: (String, String, String, String, String, String, String) -> Unit,
    onOpenLink: (String, String) -> Unit = { _, _ -> },
    onResumeBuilder: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val bottomNavItems = listOf(
        BottomNavItem("Search", Icons.Filled.Search, "home"),
        BottomNavItem("Saved", Icons.Filled.Bookmark, "saved"),
        BottomNavItem("Profile", Icons.Filled.Person, "profile")
    )

    val tabTitles = listOf("Search", "Saved", "Profile")

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val state = homeScreenState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAlertDialog by remember { mutableStateOf(false) }
    val hasActiveFilters = !state.filter.isDefault
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue)
                        .statusBarsPadding()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Menu",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                NavigationDrawerItem(
                    label = { Text("Privacy Policy", fontSize = 16.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenLink("https://joblerio.evmcstudios.com/privacy.html", "Privacy Policy")
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedTextColor = TitleDark)
                )
                NavigationDrawerItem(
                    label = { Text("Terms & Conditions", fontSize = 16.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onOpenLink("https://joblerio.evmcstudios.com/terms.html", "Terms & Conditions")
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedTextColor = TitleDark)
                )
                NavigationDrawerItem(
                    label = { Text("Resume Builder", fontSize = 16.sp) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onResumeBuilder()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedTextColor = TitleDark)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = BackgroundWhite,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = tabTitles[selectedTabIndex],
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (selectedTabIndex == 0) {
                            IconButton(onClick = { showAlertDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = { showFilterSheet = true }) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Filter",
                                        tint = Color.White
                                    )
                                    if (hasActiveFilters) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(Color.Red, CircleShape)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CardWhite,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp
                                )
                            },
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                                when (index) {
                                    1 -> Analytics.trackScreenView("Saved")
                                    2 -> Analytics.trackScreenView("Profile")
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                unselectedIconColor = TextGray,
                                unselectedTextColor = TextGray,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            when (selectedTabIndex) {
            0 -> HomeScreen(
                state = state,
                userName = userName,
                topPadding = innerPadding.calculateTopPadding(),
                bottomPadding = innerPadding.calculateBottomPadding(),
                onJobClick = onJobClick,
                onOpenLink = onOpenLink
            )
            1 -> SavedScreen(
                topPadding = innerPadding.calculateTopPadding(),
                bottomPadding = innerPadding.calculateBottomPadding(),
                onJobClick = onJobClick
            )
            2 -> ProfileScreen(
                topPadding = innerPadding.calculateTopPadding(),
                bottomPadding = innerPadding.calculateBottomPadding(),
                onLogout = onLogout
            )
            }
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = state.filter,
            onDismiss = { showFilterSheet = false },
            onApply = { newFilter ->
                state.filter = newFilter
                state.saveFilterToPrefs(context)
                scope.launch {
                    state.jobs = emptyList()
                    state.currentPage = 0
                    state.hasMore = true
                    state.isInitialLoadDone = false
                    val result = com.evmcstudios.joblerio.data.JobsApi.searchJobs(
                        query = state.searchQuery.ifBlank { "jobs" },
                        location = state.locationQuery.ifBlank { "95054" },
                        filter = newFilter,
                        start = 0,
                        limit = 10
                    )
                    result.onSuccess { searchResult ->
                        state.jobs = searchResult.jobs
                        state.totalResults = searchResult.totalResults
                        state.currentPage = 0
                        state.hasMore = state.jobs.size < searchResult.totalResults
                    }
                    state.isLoading = false
                    state.isInitialLoadDone = true
                }
            }
        )
    }

    if (showAlertDialog) {
        AlertSetupDialog(
            currentQuery = state.searchQuery,
            currentLocation = state.locationQuery,
            onDismiss = { showAlertDialog = false }
        )
    }
}

@Composable
fun ProfileScreen(topPadding: androidx.compose.ui.unit.Dp = 0.dp, bottomPadding: androidx.compose.ui.unit.Dp = 0.dp, onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val userName = remember { UserPrefs.getUserName(context) }
    val userEmail = remember { UserPrefs.getUserEmail(context) }
    val avatarUrl = remember { UserPrefs.getAvatarUrl(context) }
    val isLoggedIn = remember { UserPrefs.isLoggedIn(context) }
    val savedJobsCount = remember { SavedJobsManager.getSavedJobs(context).size }

    val clickId = remember { ReferrerManager.getClickId(context) }
    val country = remember { ReferrerManager.getCountry(context) }
    val campaign = remember { ReferrerManager.getCampaign(context) }
    val keyword = remember { ReferrerManager.getKeyword(context) }
    val rawReferrer = remember { ReferrerManager.getRawReferrer(context) }
    val postbackFired = remember { PostbackManager.isPostbackFired(context) }
    val jobClickCount = remember { PostbackManager.getJobClickCount(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(rememberScrollState())
            .padding(top = topPadding)
            .padding(24.dp)
            .padding(bottom = bottomPadding)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.ic_empty_jobs),
                        placeholder = painterResource(id = R.drawable.ic_empty_jobs)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(PrimaryBlue.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userName.isNotBlank()) userName.first().uppercaseChar().toString() else "?",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = userName.ifBlank { "Guest User" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TitleDark
                    )
                    if (userEmail.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userEmail,
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Activity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleDark
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Saved Jobs",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TitleDark
                        )
                        Text(
                            text = "$savedJobsCount jobs saved",
                            fontSize = 12.sp,
                            color = TextGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val recentSearches = remember { RecentSearchManager.getSearches(context) }
        if (recentSearches.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Recent Searches",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    recentSearches.take(5).forEach { search ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                tint = TextGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = search.query,
                                    fontSize = 14.sp,
                                    color = TitleDark
                                )
                                Text(
                                    text = search.location.ifBlank { "All locations" },
                                    fontSize = 12.sp,
                                    color = TextGray
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Referrer Info",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TitleDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                ReferrerRow("Click ID", clickId.ifBlank { "N/A" })
                ReferrerRow("Country", country.ifBlank { "N/A" })
                ReferrerRow("Campaign", campaign.ifBlank { "N/A" })
                ReferrerRow("Keyword", keyword.ifBlank { "N/A" })
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                ReferrerRow("Postback Fired", if (postbackFired) "Yes" else "No")
                ReferrerRow("Job Clicks", jobClickCount.toString())
                ReferrerRow("Raw Referrer", rawReferrer.ifBlank { "N/A" })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isLoggedIn) {
                    UserPrefs.clear(context)
                }
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLoggedIn) Color(0xFFFF6B6B) else PrimaryBlue
            )
        ) {
            Icon(
                imageVector = if (isLoggedIn) Icons.Filled.ExitToApp else Icons.Filled.PersonAdd,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isLoggedIn) "Log Out" else "Log In",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ReferrerRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextGray
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TitleDark
        )
    }
}
