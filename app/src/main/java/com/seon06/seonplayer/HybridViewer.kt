package com.seon06.seonplayer

import android.annotation.SuppressLint
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.session.MediaController
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@UnstableApi
@Composable
fun HybridViewer(hybridViewModel: HybridViewModel, controller: ListenableFuture<MediaController>){
    val context = LocalContext.current
    val transition = updateTransition(targetState = hybridViewModel.isMini, label = "")

    fun load(data: returnVideoLoader, loadMediaItem: Boolean = true){
        hybridViewModel.setVideoData(data.videoData)
        hybridViewModel.setSubtitles(data.lyricListItem)
        hybridViewModel.setSubtitle(getLyric(hybridViewModel.lang.value, hybridViewModel.subtitles.value).lyrics)
        hybridViewModel.setTagItem(data.tagItem)
        for (l in hybridViewModel.subtitles.value.iterator()){
            val sub = l.lyrics
            for ((index_,i) in sub.iterator().withIndex()){
                /*
                players.createMessage{ messageType, payload ->
                    if (hybridViewModel.lang.value == l.lang) {
                        var position = players.currentPosition + 0
                        if (index_ > 0 && sub[index_ - 1].startMS + sub[index_ - 1].durationMS == i.startMS) position += 1
                        hybridViewModel.setIndex(findTargetLyric(sub, position))
                    }
                }.setLooper(Looper.getMainLooper())
                    .setPosition(i.startMS.toLong())
                    .setDeleteAfterDelivery(false)
                    .send()
                players.createMessage{ messageType, payload ->
                    if (hybridViewModel.lang.value == l.lang) hybridViewModel.setIndex(findTargetLyric(sub,players.currentPosition))
                }.setLooper(Looper.getMainLooper())
                    .setPosition(i.startMS.toLong()+i.durationMS)
                    .setDeleteAfterDelivery(false)
                    .send()*/
            }
        }
        if (loadMediaItem) {
            hybridViewModel.setMediaSource(data.mediaSource)
            controller.get().setMediaItem(hybridViewModel.mediaSource.value!!.mediaItem)
        }
        controller.get().prepare()
        controller.get().playWhenReady = true
        Log.i("KJJKJKJKJK", "Load!!")
    }

    val playerTransition = updateTransition(targetState = hybridViewModel.isMini, label = "")
    val width = LocalConfiguration.current.screenWidthDp.dp

    val playerWidth by playerTransition.animateDp (
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow) },
        label = "width")
    { state ->
        if (!state.value) width
        else (16 * 80.dp) / 9
    }

    val height = LocalConfiguration.current.screenHeightDp.dp

    val yOffset by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow) },
        label = "OffsetY"
    ) { state ->
        if (!state.value) {
            toDp(WindowInsets.systemBars.getTop(LocalDensity.current))
        }
        else {
            var a = toDp(WindowInsets.ime.getBottom(
                LocalDensity.current))
            if (a>0.dp) a -= 80.dp + toDp(WindowInsets.systemBars.getBottom(LocalDensity.current))
            height-toDp(hybridViewModel.bottomBarHeight.value) + (-80).dp + toDp(WindowInsets.systemBars.getBottom(LocalDensity.current))+
                    toDp(WindowInsets.systemBars.getTop(LocalDensity.current))- a
        }
    }

    val height_ by transition.animateDp(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow) },
        label = "OffsetY"
    ) { state ->
        if (!state.value) height
        else 80.dp
    }

    val backgroundT = updateTransition(targetState = hybridViewModel.isMini)
    val background by backgroundT.animateColor(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow) },
        label = "background"
    ){ state ->
        if (state.value) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.background
    }

    val color = MaterialTheme.colorScheme.tertiaryContainer
    LaunchedEffect(hybridViewModel.videoId.value){
        hybridViewModel.setIsloaded(false)
        if (hybridViewModel.videoId.value!=null && hybridViewModel.videoId.value.isNotBlank()){
            controller.get().clearMediaItems()
            //if (players.isReleased) players.prepare()
            if (!hybridViewModel.isloaded.value) GlobalScope.launch  {
                val data = VideoLoader(hybridViewModel.videoId.value, context, color)
                withContext(Dispatchers.Main) { load(data) }
                hybridViewModel.setIsloaded(true)
            }
        }
    }

    LaunchedEffect(hybridViewModel.playlist.value) {
        if (hybridViewModel.playlist.value.isNotEmpty()){
            Log.i("playlistView", "Loaded!")
            hybridViewModel.setOpenVideo(true)
            hybridViewModel.setIsMini(false)
            hybridViewModel.setIsloaded(false)
            controller.get().clearMediaItems()
            //controller.get().setMediaSources(hybridViewModel.playlist.value)
            val data: returnVideoLoader? = controller.get().currentMediaItem!!.localConfiguration!!.tag as returnVideoLoader?
            if (data != null) {
                load(data, false)
            }
            controller.get().prepare()
            controller.get().playWhenReady = true
            controller.get().addListener(object : Player.Listener{
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (mediaItem?.localConfiguration != null) {
                        val data = mediaItem.localConfiguration!!.tag as returnVideoLoader?
                        if (data != null) {
                            load(data, false)
                        }
                    }
                }
            }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            //players.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height_)
            .zIndex(if (hybridViewModel.openVideo.value) 1f else -1f)
            .alpha(if (hybridViewModel.openVideo.value) 1f else 0f)
            .offset { IntOffset(0, yOffset.roundToPx()) }
            .absoluteOffset()
            .background(background)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 50) {
                        hybridViewModel.setIsMini(true)

                    } else if (dragAmount < 0) {
                        //changOpenVideo(true)
                        hybridViewModel.setIsMini(false)
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (abs(dragAmount) > 50 && hybridViewModel.isMini.value) {
                        controller.get().clearMediaItems()
                        hybridViewModel.setOpenVideo(false)
                        hybridViewModel.setIsMini(false)
                        hybridViewModel.setVideoId("")
                    }
                }
            }
    ){
        Column {
            Row {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = controller.get()
                            keepScreenOn = true
                        }
                    },
                    modifier = Modifier
                        .width(playerWidth)
                        .aspectRatio(16 / 9f)
                )
                if (hybridViewModel.isMini.value) {
                    Column {
                        Text(hybridViewModel.videoData.value.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(hybridViewModel.videoData.value.channel, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                }
            }

            val state = rememberPagerState(pageCount={2})
            var expand by remember { mutableStateOf(false) }
            if (!hybridViewModel.isMini.value) {
                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxWidth()
                        .clickable { expand = !expand }
                ) {
                    Column (modifier = Modifier
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                        .verticalScroll(rememberScrollState())) {
                        HybridViewTitleView(state, hybridViewModel, expand, controller.get())
                    }
                }
                Text(hybridViewModel.videoData.value.channel, modifier = Modifier
                    .padding(5.dp,0.dp,0.dp,0.dp))

                SubtitleView(hybridViewModel.subtitle.value, hybridViewModel.index.value)
            }
        }

        //if (players != null) MediaNotification(players,hybridViewModel.videoData.value)
        BackHandler(!hybridViewModel.isMini.value) {
            hybridViewModel.setIsMini(true)
        }
    }
}