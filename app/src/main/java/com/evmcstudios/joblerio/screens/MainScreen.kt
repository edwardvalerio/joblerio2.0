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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.evmcstudios.joblerio.data.Analytics
import com.evmcstudios.joblerio.data.SavedJobsManager
import com.evmcstudios.joblerio.data.UserPrefs
import com.evmcstudios.joblerio.ui.theme.BackgroundWhite
import com.evmcstudios.joblerio.ui.theme.CardWhite
import com.evmcstudios.joblerio.ui.theme.PrimaryBlue
import com.evmcstudios.joblerio.ui.theme.TextGray
import com.evmcstudios.joblerio.ui.theme.TitleDark

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MainScreen(
    userName: String = "",
    onJobClick: (String, String, String, String, String, String, String) -> Unit,
    onLogout: () -> Unit = {}
) {
    val bottomNavItems = listOf(
        BottomNavItem("Search", Icons.Filled.Search, "home"),
        BottomNavItem("Saved", Icons.Filled.Bookmark, "saved"),
        BottomNavItem("Profile", Icons.Filled.Person, "profile")
    )

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val homeScreenState = rememberHomeScreenState()

    Scaffold(
        containerColor = BackgroundWhite,
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
                state = homeScreenState,
                userName = userName,
                bottomPadding = innerPadding.calculateBottomPadding(),
                onJobClick = onJobClick
            )
            1 -> SavedScreen(
                bottomPadding = innerPadding.calculateBottomPadding(),
                onJobClick = onJobClick
            )
            2 -> ProfileScreen(onLogout = onLogout)
        }
    }
}

@Composable
fun ProfileScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    val userName = remember { UserPrefs.getUserName(context) }
    val userEmail = remember { UserPrefs.getUserEmail(context) }
    val avatarUrl = remember { UserPrefs.getAvatarUrl(context) }
    val isLoggedIn = remember { UserPrefs.isLoggedIn(context) }
    val savedJobsCount = remember { SavedJobsManager.getSavedJobs(context).size }

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
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
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
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
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
}
