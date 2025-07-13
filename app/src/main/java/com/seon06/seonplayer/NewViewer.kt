package com.seon06.seonplayer


import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(UnstableApi::class)
@Composable
fun NewViewer(id: String){
    Column {
        val context = LocalContext.current
        var isloaded by remember { mutableStateOf(false) }
        val players = remember {
            ExoPlayer.Builder(context).apply {
                setLoadControl(DefaultLoadControl.Builder().apply {
                    setBufferDurationsMs(3000, 10000, 3000, 500)
                }.build())
                setRenderersFactory(DefaultRenderersFactory(context).apply {
                    setEnableDecoderFallback(true)
                })
            }.build()
        }
        val color = MaterialTheme.colorScheme.tertiaryContainer

        var index by remember { mutableStateOf(-1) }
        var subtitles: List<LyricItem> by remember { mutableStateOf(emptyList()) }
        var videoData: VideoData by remember { mutableStateOf(VideoData("","","",false,"")) }
        var tagItem: List<TagItem> by remember { mutableStateOf(emptyList()) }

        val lang: String by remember { mutableStateOf("ko") }

        if (!isloaded) GlobalScope.launch  {
            val data = VideoLoader(id, context, color)
            val mediaSource = data.mediaSource
            videoData = data.videoData
            subtitles = getLyric(lang, data.lyricListItem).lyrics
            tagItem = data.tagItem
            withContext(Dispatchers.Main) {
                players.setMediaSource(mediaSource)
                players.prepare()
                players.playWhenReady = true
            }
            isloaded = true

        }

        LaunchedEffect (Unit) {
            if (!isloaded) while (true) {
                delay(1L)
                index = findTargetLyric(subtitles, players.currentPosition)
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                players.release()
            }
        }
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = players
                    keepScreenOn = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
        )
        Text(videoData.title)
        LazyRow {
            items(tagItem){ tag ->
                Button(onClick = {}) {
                    Text(tag.name)
                }
            }
        }
        Row {
            Text(videoData.channel)
            if (videoData.verified) Icon(Icons.Filled.CheckCircle,"verified")
        }

        SubtitleView(subtitles, index)
    }
}