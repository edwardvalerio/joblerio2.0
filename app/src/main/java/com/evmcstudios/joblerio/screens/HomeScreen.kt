package com.evmcstudios.joblerio.screens

import android.content.Context
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evmcstudios.joblerio.data.Analytics
import com.evmcstudios.joblerio.data.Job
import com.evmcstudios.joblerio.data.JobsApi
import com.evmcstudios.joblerio.data.PostbackManager
import com.evmcstudios.joblerio.data.ReferrerManager
import com.evmcstudios.joblerio.data.RemoteConfigManager
import com.evmcstudios.joblerio.data.SavedJobsManager
import com.evmcstudios.joblerio.ui.theme.BackgroundWhite
import com.evmcstudios.joblerio.ui.theme.CardWhite
import com.evmcstudios.joblerio.ui.theme.PrimaryBlue
import com.evmcstudios.joblerio.ui.theme.SearchBarBg
import com.evmcstudios.joblerio.ui.theme.TextGray
import com.evmcstudios.joblerio.ui.theme.TitleDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeScreenState(
    jobs: List<Job> = emptyList(),
    isLoading: Boolean = true,
    isLoadingMore: Boolean = false,
    errorMessage: String? = null,
    totalResults: Int = 0,
    searchQuery: String = "",
    locationQuery: String = ""
) {
    var jobs by mutableStateOf(jobs)
        internal set
    var isLoading by mutableStateOf(isLoading)
        internal set
    var isLoadingMore by mutableStateOf(isLoadingMore)
        internal set
    var errorMessage by mutableStateOf(errorMessage)
        internal set
    var totalResults by mutableIntStateOf(totalResults)
        internal set
    var searchQuery by mutableStateOf(searchQuery)
        internal set
    var locationQuery by mutableStateOf(locationQuery)
        internal set
    internal var currentPage by mutableIntStateOf(0)
    internal var hasMore by mutableStateOf(true)
    internal var isInitialLoadDone by mutableStateOf(false)
    internal var hasAttemptedInitialLoad by mutableStateOf(false)
    internal var initialLoadComplete by mutableStateOf(false)
    var scrollIndex by mutableIntStateOf(0)
        internal set
    var scrollOffset by mutableIntStateOf(0)
        internal set

    fun saveToPrefs(context: android.content.Context) {
        context.getSharedPreferences("home_state", android.content.Context.MODE_PRIVATE).edit()
            .putString("search_query", searchQuery)
            .putString("location_query", locationQuery)
            .putBoolean("initial_load_complete", initialLoadComplete)
            .apply()
    }

    fun loadFromPrefs(context: android.content.Context) {
        val prefs = context.getSharedPreferences("home_state", android.content.Context.MODE_PRIVATE)
        searchQuery = prefs.getString("search_query", "") ?: ""
        locationQuery = prefs.getString("location_query", "") ?: ""
        initialLoadComplete = prefs.getBoolean("initial_load_complete", false)
        hasAttemptedInitialLoad = initialLoadComplete
    }
}

@Composable
fun rememberHomeScreenState(): HomeScreenState {
    val context = LocalContext.current
    return remember {
        HomeScreenState().also {
            it.loadFromPrefs(context)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeScreenState,
    userName: String = "",
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp,
    onJobClick: (String, String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _, _ -> },
    onOpenLink: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.scrollIndex,
        initialFirstVisibleItemScrollOffset = state.scrollOffset
    )

    fun loadJobs(query: String, location: String, loadMore: Boolean = false) {
        Log.d("HomeScreen", "loadJobs called: query=$query, location=$location, loadMore=$loadMore, jobs=${state.jobs.size}, isLoading=${state.isLoading}")
        scope.launch {
            if (loadMore) {
                state.isLoadingMore = true
            } else {
                state.isLoading = true
                state.currentPage = 0
                state.hasMore = true
                state.jobs = emptyList()
                state.isInitialLoadDone = false
            }
            state.errorMessage = null

            val pageToLoad = if (loadMore) state.currentPage + 1 else 0
            val result = JobsApi.searchJobs(
                query = query.ifBlank { "jobs" },
                location = location.ifBlank { "95054" },
                start = pageToLoad * 10,
                limit = 10
            )
            result.onSuccess { searchResult ->
                if (loadMore) {
                    state.jobs = state.jobs + searchResult.jobs
                } else {
                    state.jobs = searchResult.jobs
                }
                state.totalResults = searchResult.totalResults
                state.currentPage = pageToLoad
                state.hasMore = state.jobs.size < searchResult.totalResults
            }.onFailure { e ->
                state.errorMessage = e.message ?: "Failed to load jobs"
            }
            state.isLoading = false
            state.isInitialLoadDone = true
            Log.d("HomeScreen", "loadJobs finished: jobs=${state.jobs.size}, isInitialLoadDone=${state.isInitialLoadDone}")
            if (loadMore) {
                delay(500)
                state.isLoadingMore = false
            }
        }
    }

    var permissionResult by remember { mutableStateOf<Boolean?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        Log.d("HomeScreen", "Location permission result: granted=$granted")
        permissionResult = granted
    }

    DisposableEffect(Unit) {
        Log.d("HomeScreen", "DisposableEffect composed, jobs=${state.jobs.size}")
        onDispose {
            Log.d("HomeScreen", "DisposableEffect disposing, saving scroll: index=${listState.firstVisibleItemIndex}, offset=${listState.firstVisibleItemScrollOffset}")
            state.scrollIndex = listState.firstVisibleItemIndex
            state.scrollOffset = listState.firstVisibleItemScrollOffset
        }
    }

    val isSearchCollapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset > 300)
        }
    }

    fun resolveKeyword(referrerKeyword: String, countryCode: String) {
        val referrerCountry = ReferrerManager.getCountry(context)
        val effectiveCountry = countryCode.ifBlank { referrerCountry }
        val remoteKeyword = if (effectiveCountry.isNotBlank()) RemoteConfigManager.getKeywordForCountry(effectiveCountry) else ""
        val searchKeyword = referrerKeyword.ifBlank { remoteKeyword }.ifBlank { "Jobs" }
        Log.d("HomeScreen", "Keyword: referrer='$referrerKeyword', country='$effectiveCountry', remote='$remoteKeyword', final='$searchKeyword'")
        if (state.searchQuery.isBlank()) {
            state.searchQuery = searchKeyword
        }
    }

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "Initial load started")
        if (!state.initialLoadComplete && !state.hasAttemptedInitialLoad) {
            state.hasAttemptedInitialLoad = true

            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                Log.d("HomeScreen", "Permission already granted, detecting location...")
                val locationResult = JobsApi.detectLocation(context)
                if (locationResult != null) {
                    Log.d("HomeScreen", "Location: '${locationResult.location}', country: '${locationResult.countryCode}'")
                    if (locationResult.location.isNotBlank()) state.locationQuery = locationResult.location
                    resolveKeyword("", locationResult.countryCode)
                } else {
                    resolveKeyword("", "")
                }
                loadJobs(state.searchQuery.ifBlank { "jobs" }, state.locationQuery.ifBlank { "95054" })
                state.initialLoadComplete = true
                state.saveToPrefs(context)
            } else {
                Log.d("HomeScreen", "No permission, requesting...")
                permissionResult = null
                locationPermissionLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            }
        } else if (state.initialLoadComplete && state.jobs.isEmpty()) {
            Log.d("HomeScreen", "Restored state, loading saved queries")
            loadJobs(state.searchQuery.ifBlank { "jobs" }, state.locationQuery.ifBlank { "95054" })
        }
    }

    LaunchedEffect(permissionResult) {
        if (state.initialLoadComplete) return@LaunchedEffect
        if (permissionResult == null) return@LaunchedEffect

        Log.d("HomeScreen", "Permission result received: $permissionResult")

        if (permissionResult == true) {
            val locationResult = JobsApi.detectLocation(context)
            if (locationResult != null) {
                Log.d("HomeScreen", "Location after permission: '${locationResult.location}', country: '${locationResult.countryCode}'")
                if (locationResult.location.isNotBlank()) state.locationQuery = locationResult.location
                resolveKeyword("", locationResult.countryCode)
            } else {
                resolveKeyword("", "")
            }
        } else {
            Log.d("HomeScreen", "Permission denied, using defaults")
            resolveKeyword("", "")
        }

        loadJobs(state.searchQuery.ifBlank { "jobs" }, state.locationQuery.ifBlank { "95054" })
        state.initialLoadComplete = true
        state.saveToPrefs(context)
    }

    LaunchedEffect(listState.layoutInfo) {
        if (!state.isInitialLoadDone || state.isLoading || state.isLoadingMore || !state.hasMore || state.jobs.isEmpty()) return@LaunchedEffect
        val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val totalItems = listState.layoutInfo.totalItemsCount
        Log.d("HomeScreen", "Infinite scroll check: lastVisible=$lastVisibleItem, total=$totalItems")
        if (lastVisibleItem >= totalItems - 3) {
            Log.d("HomeScreen", "Triggering loadMore")
            loadJobs(state.searchQuery.ifBlank { "jobs" }, state.locationQuery.ifBlank { "95054" }, loadMore = true)
        }
    }

    var showMenuSheet by remember { mutableStateOf(false) }
    val menuSheetState = rememberModalBottomSheetState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
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
                IconButton(onClick = { showMenuSheet = true }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Search",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = Color.White
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !isSearchCollapsed,
            enter = expandVertically(
                animationSpec = tween(300)
            ),
            exit = shrinkVertically(
                animationSpec = tween(300)
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { state.searchQuery = it },
                        placeholder = { Text("Search job, company", color = TextGray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextGray
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedContainerColor = SearchBarBg,
                            focusedContainerColor = SearchBarBg
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.locationQuery,
                        onValueChange = { state.locationQuery = it },
                        placeholder = { Text("Location", color = TextGray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = TextGray
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedContainerColor = SearchBarBg,
                            focusedContainerColor = SearchBarBg
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            Analytics.trackSearch(state.searchQuery, state.locationQuery)
                            loadJobs(state.searchQuery, state.locationQuery)
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue),
                    ) {
                        Text(
                            text = "Search",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                }
            }
        }
    }

    AnimatedVisibility(
            visible = isSearchCollapsed,
            enter = expandVertically(tween(300)),
            exit = shrinkVertically(tween(300))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { state.searchQuery = it },
                        placeholder = { Text("Search job, company", color = TextGray, fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            Analytics.trackSearch(state.searchQuery, state.locationQuery)
                            loadJobs(state.searchQuery, state.locationQuery)
                            scope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Job Listings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TitleDark
                    )
                    if (state.totalResults > 0) {
                        Text(
                            text = "${state.jobs.size} of ${state.totalResults}",
                            fontSize = 13.sp,
                            color = TextGray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            if (state.errorMessage != null && state.jobs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardWhite)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Something went wrong",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TitleDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.errorMessage ?: "",
                                fontSize = 13.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { loadJobs(state.searchQuery, state.locationQuery) }) {
                                Text("Retry", color = PrimaryBlue)
                            }
                        }
                    }
                }
            }

            if (!state.isLoading && state.errorMessage == null && state.jobs.isEmpty() && state.isInitialLoadDone) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No jobs found",
                            fontSize = 16.sp,
                            color = TextGray
                        )
                    }
                }
            }

            items(state.jobs) { job ->
                JobCard(
                    job = job,
                    context = context,
                    onClick = {
                        PostbackManager.incrementJobClickCount(context)
                        scope.launch {
                            PostbackManager.checkAndFirePostback(context)
                        }
                        onJobClick(job.url, job.title, job.company, job.city, job.state, job.date, job.snippet)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (state.isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(bottomPadding + 16.dp))
            }
        }
    }

    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            sheetState = menuSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Menu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleDark
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Privacy Policy",
                    fontSize = 16.sp,
                    color = PrimaryBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMenuSheet = false
                            onOpenLink("https://joblerio.evmcstudios.com/privacy.html", "Privacy Policy")
                        }
                        .padding(vertical = 14.dp)
                )

                Text(
                    text = "Terms & Conditions",
                    fontSize = 16.sp,
                    color = PrimaryBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showMenuSheet = false
                            onOpenLink("https://joblerio.evmcstudios.com/terms.html", "Terms & Conditions")
                        }
                        .padding(vertical = 14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun JobCard(
    job: Job,
    context: Context,
    onClick: () -> Unit
) {
    var isSaved by remember { mutableStateOf(SavedJobsManager.isJobSaved(context, job.id)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = job.companyInitial,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.company,
                        fontSize = 13.sp,
                        color = TextGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {
                        if (isSaved) {
                            SavedJobsManager.removeJob(context, job)
                            Analytics.trackJobUnsave(job.title, job.company)
                        } else {
                            SavedJobsManager.saveJob(context, job)
                            Analytics.trackJobSave(job.title, job.company)
                        }
                        isSaved = !isSaved
                    }
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) "Unsave" else "Save",
                        tint = if (isSaved) PrimaryBlue else TextGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            if (job.snippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = job.snippet,
                    fontSize = 12.sp,
                    color = TextGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = TextGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.location,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }
                Text(
                    text = job.date,
                    fontSize = 12.sp,
                    color = TextGray
                )
            }
        }
    }
}
