package com.seon06.seonplayer

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun Playlist(hybridViewModel: HybridViewModel,playListViewModel: PlayListViewModel, playlistString: String?) {
    val playlist: PlaylistList = playListViewModel.flow.collectAsState(initial = PlaylistList.getDefaultInstance()).value
    var apfyURLopen by remember { mutableStateOf(false) }
    var apfyURLtext by remember { mutableStateOf("") }
    var apfyButton by remember { mutableStateOf(true) }
    val context = LocalContext.current

    fun loadPlaylist(url: String){
        Thread {
            apfyButton = false
            try {
                val data = PlaylistLoader(url, context)
                if (data != null) {
                    playListViewModel.addPlayList(data)
                }
                apfyButton = true
            } catch (e: Exception) {
                Log.e("PlaylistLoader", e.toString())
                apfyButton = true
            }
        }.start()
    }

    LaunchedEffect(Unit) {
        playlistString?.let {
            loadPlaylist(playlistString)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(stringResource(R.string.playlist))

        Column {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
            ) {
                itemsIndexed(playlist.itemsList) { index, i ->
                    PlaylistItemView(index, i, hybridViewModel, playlist, playListViewModel)
                    if (hybridViewModel.isMini.value && playlist.itemsCount - 1 == index) {
                        Spacer(
                            modifier = Modifier
                                .padding(toDp(80))
                        )
                    }
                }
            }
            Surface(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        apfyURLopen = !apfyURLopen
                    }
            ) {
                Column(
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.tertiaryContainer
                    ).animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                ){
                    Text(stringResource(R.string.add_playlist_from_yturl), modifier = Modifier.padding(5.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                    if (apfyURLopen){
                        Row(
                            modifier = Modifier
                                .padding(15.dp,5.dp,15.dp,20.dp)
                        ) {
                            TextField(apfyURLtext, onValueChange = { t->
                                apfyURLtext = t
                            }, modifier = Modifier.weight(1f))
                            Button(
                                onClick = { loadPlaylist(apfyURLtext) },
                                enabled = apfyButton,
                                modifier = Modifier
                                    .height(50.dp)
                            ) { Text("Add") }
                        }

                    }
                }

            }
        }
    }
}