package com.seon06.seonplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.common.util.concurrent.ListenableFuture

/* 참고
* https://medium.com/@bharadwaj.rns/bottom-navigation-in-jetpack-compose-using-material3-c153ccbf0593
*/

@Composable
fun toDp(intValue: Int): Dp {
    val density = LocalDensity.current
    val a = with(density) { intValue.toDp() }
    return a
}

@SuppressLint("RememberReturnType")
@androidx.annotation.OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun BottomNavigationBar(playListViewModel: PlayListViewModel, controller: ListenableFuture<MediaController>) {
    var selected by remember {
        mutableStateOf(0)
    }

    val navController = rememberNavController()

    val hybridViewModel by remember { mutableStateOf(HybridViewModel()) }
    val context = LocalContext.current
    val intent = remember { context.findActivity()?.intent }
    val sharedText = remember {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            null
        }
    }

    var start = navigationItems.home.route

    // sharedText가 null이 아니면 YouTube URL을 처리합니다.
    sharedText?.let { url ->
        // URL 파싱 및 필요한 정보 추출 (예: 동영상 ID)
        Log.i("SEND_YOUTUBE", url)
        start = navigationItems.playlist.route
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.onGloballyPositioned { i-> hybridViewModel.setBottomBarHeight(i.size.height)}
            ) {
                BottomNavigationItem().bottomNavigationItems().forEachIndexed {index,navigationItem ->
                    NavigationBarItem(
                        selected = index == selected,
                        label = {
                            Text(navigationItem.title)
                        },
                        icon = {
                            Icon(
                                navigationItem.icon,
                                contentDescription = navigationItem.title
                            )
                        },
                        onClick = {
                            selected = index
                            navController.navigate(navigationItem.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        HybridViewer(hybridViewModel, controller)
        NavHost(
            navController = navController,
            startDestination = start,
            enterTransition = {
                fadeIn(tween(300)) +
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(tween(300))+
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
            },
            modifier = Modifier.padding(paddingValues = paddingValues)) {
            composable(navigationItems.home.route) {
                Home(hybridViewModel)
            }
            composable(navigationItems.playlist.route) {
                Playlist(hybridViewModel,playListViewModel, sharedText)
            }
            composable(navigationItems.search.route) {
                Search(hybridViewModel)
            }
            composable(navigationItems.profile.route) {
                Profile()
            }
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}