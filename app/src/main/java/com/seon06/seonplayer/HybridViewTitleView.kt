package com.seon06.seonplayer

import androidx.annotation.OptIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.session.MediaController
import coil.compose.AsyncImage

@OptIn(UnstableApi::class)
@Composable
fun HybridViewTitleView(state: PagerState, hybridViewModel: HybridViewModel, expand: Boolean,players: MediaController){
    HorizontalPager(state) { page ->
        if (page==0) {
            Column(
                modifier = Modifier.heightIn(max=250.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    hybridViewModel.videoData.value.title, modifier = Modifier
                        .padding(10.dp)
                )
                if (expand) {
                    LazyRow {
                        items(hybridViewModel.tagItem.value) { tag ->
                            OutlinedButton(
                                onClick = {},
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(5.dp, 0.dp),
                                colors = ButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    disabledContentColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.tertiary,
                                ),
                                contentPadding = PaddingValues(0.dp),
                                border = BorderStroke(0.dp, Color.Transparent)
                            ) {
                                Text(
                                    tag.name,
                                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                                    modifier = Modifier
                                        .padding(15.dp, 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        hybridViewModel.videoData.value.description,
                        modifier = Modifier
                            .padding(5.dp),
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                    )
                    LazyRow {
                        items(hybridViewModel.subtitles.value) { i ->
                            OutlinedButton(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(5.dp, 0.dp),
                                onClick = {
                                    hybridViewModel.setLang(i.lang)
                                    hybridViewModel.setSubtitle(
                                        getLyric(
                                            hybridViewModel.lang.value,
                                            hybridViewModel.subtitles.value
                                        ).lyrics
                                    )
                                    hybridViewModel.setIndex(
                                        findTargetLyric(
                                            hybridViewModel.subtitle.value,
                                            players.currentPosition
                                        )
                                    )
                                },
                                contentPadding = PaddingValues(5.dp)
                            ) {
                                Text(
                                    getDisplayLanguage(i.lang),
                                    modifier = Modifier.padding(0.dp),
                                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                                )
                            }
                        }
                    }
                }
            }
        } else
        {
            Column {
                Row {
                    AsyncImage("","")
                    Column {
                        val index = players.nextMediaItemIndex
                        fun get(i:Int): MediaSource?{
                            try {
                                return hybridViewModel.playlist.value[i]
                            } catch (e: Exception) {
                                return null
                            }
                        }
                        val mediaData: MediaSource? = get(index)
                        var title = "No video"
                        var channel = "No Video"
                        if (mediaData != null){
                            val r = mediaData.mediaItem.localConfiguration!!.tag as returnVideoLoader?
                            if (r != null){
                                title = r.videoData.title
                                channel = r.videoData.channel
                            }
                        }
                        Text(stringResource(R.string.next_video), fontSize = 12.sp, modifier = Modifier.padding(10.dp,5.dp,0.dp,0.dp))
                        Text(title, overflow = TextOverflow.Ellipsis, maxLines = 1, modifier = Modifier.padding(10.dp,0.dp,0.dp,0.dp))
                        Text(channel, overflow = TextOverflow.Ellipsis, maxLines = 1, modifier = Modifier.padding(10.dp,0.dp,0.dp,0.dp))
                    }
                }
                if (expand) {
                    Text(
                        stringResource(R.string.playlist),
                        modifier = Modifier.padding(10.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                    ) {
                        itemsIndexed(hybridViewModel.playlist.value) { index, i ->
                            val mediaItem = i.mediaItem.localConfiguration
                            if (mediaItem != null){
                                val r = mediaItem.tag as returnVideoLoader?
                                if (r != null){
                                    val videoData = r.videoData
                                    Card (
                                        modifier = Modifier
                                            .padding(5.dp),
                                        onClick = {
                                            players.seekTo(index,0)
                                        },
                                        colors = CardDefaults.cardColors(
                                            if (players.currentMediaItemIndex!=index) CardDefaults.cardColors().containerColor else MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    ) {
                                        Text(videoData.title, overflow = TextOverflow.Ellipsis, maxLines = 1)
                                        Text(videoData.channel, overflow = TextOverflow.Ellipsis, maxLines = 1)
                                    }
                                }
                            } else Spacer(modifier = Modifier.padding(0.dp))
                        }
                    }
                }
            }
        }
    }
}