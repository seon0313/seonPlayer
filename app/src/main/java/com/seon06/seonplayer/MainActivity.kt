package com.seon06.seonplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModelProvider
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.seon06.seonplayer.ui.theme.SeonPlayerTheme

private val Context.playlistDataStore: DataStore<PlaylistList> by dataStore(
    fileName = "playlist.pb",
    serializer = PlayListSerializer
)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionToken = SessionToken(this, ComponentName(this, PlayerService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        enableEdgeToEdge()
        setContent {

            SeonPlayerTheme {
                val playlistViewModel = ViewModelProvider(
                    this,
                    PlayListViewModelFactory(PlayListRepository(playlistDataStore))
                )[PlayListViewModel::class.java]
                BottomNavigationBar(playlistViewModel,controllerFuture)
            }
        }
    }
}