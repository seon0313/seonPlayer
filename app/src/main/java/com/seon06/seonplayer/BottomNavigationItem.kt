package com.seon06.seonplayer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

data class BottomNavigationItem(
    val title : String = "",
    val icon : ImageVector = Icons.Filled.Home,
    val route : String = ""
) {
    @Composable
    fun bottomNavigationItems() : List<BottomNavigationItem> {
        return listOf(
            BottomNavigationItem(
                title = stringResource(R.string.home),
                icon = Icons.Filled.Home,
                route = navigationItems.home.route
            ),
            BottomNavigationItem(
                title = stringResource(R.string.playlist),
                icon = Icons.Filled.Menu,
                route = navigationItems.playlist.route
            ),
            BottomNavigationItem(
                title = stringResource(R.string.search),
                icon = Icons.Filled.Search,
                route = navigationItems.search.route
            ),
            BottomNavigationItem(
                title = stringResource(R.string.profile),
                icon = Icons.Filled.Person,
                route = navigationItems.profile.route
            ),
        )
    }
}