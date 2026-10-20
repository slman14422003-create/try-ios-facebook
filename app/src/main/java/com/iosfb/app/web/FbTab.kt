package com.iosfb.app.web

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PlayCircle
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
    HOME("Home", "Home", "/", Icons.Outlined.Home, Icons.Rounded.Home),
    FRIENDS("Friends", "Friends", "/friends/", Icons.Outlined.People, Icons.Rounded.People),
    WATCH("Watch", "Watch", "/watch/", Icons.Outlined.PlayCircle, Icons.Rounded.PlayCircle),
    ALERTS("Alerts", "Notifications", "/notifications/", Icons.Outlined.Notifications, Icons.Rounded.Notifications),
    MENU("Menu", "Menu", "/bookmarks/", Icons.Rounded.Menu, Icons.Rounded.Menu),
}
