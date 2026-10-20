package com.iosfb.app.web

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.ui.graphics.vector.ImageVector

const val BASE_URL = "https://m.facebook.com"

enum class FbTab(
    val label: String,
    val title: String,
    val path: String,
    val icon: ImageVector,
    val iconSelected: ImageVector,
) {
    HOME("Home", "Home", "/", Icons.Outlined.Home, Icons.Filled.Home),
    FRIENDS("Friends", "Friends", "/friends/", Icons.Outlined.People, Icons.Filled.People),
    WATCH("Watch", "Watch", "/watch/", Icons.Outlined.PlayCircle, Icons.Filled.PlayCircle),
    ALERTS("Alerts", "Notifications", "/notifications/", Icons.Outlined.Notifications, Icons.Filled.Notifications),
    MENU("Menu", "Menu", "/bookmarks/", Icons.Filled.Menu, Icons.Filled.Menu),
}
