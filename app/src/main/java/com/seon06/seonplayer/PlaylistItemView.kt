package com.seon06.seonplayer

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.source.MediaSource
import coil.compose.AsyncImage
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject

@Composable
fun PlaylistItemView(index: Int, i: PlaylistData,hybridViewModel: HybridViewModel,playlist: PlaylistList, playListViewModel: PlayListViewModel){
    var opened by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val color = MaterialTheme.colorScheme.tertiaryContainer
    Card(modifier = Modifier
        .padding(5.dp)
        .clickable { opened = !opened }) {
        Column(
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(0.dp, 15.dp)
                    .height(80.dp)
                    .fillMaxWidth()
            ) {
                var url = ""
                if (i.itemsCount > 0) url = i.getItems(0).thumbnailURL
                AsyncImage(
                    url, "thumbnail",
                    modifier = Modifier
                        .aspectRatio(16 / 9f)
                        .fillMaxHeight()
                        .padding(15.dp, 0.dp, 0.dp, 0.dp)
                )
                Column {
                    Text(i.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(i.uploader, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (opened) {
                Row {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp, 0.dp, 0.dp, 0.dp),
                        shape = RoundedCornerShape(5.dp),
                        onClick = {
                            Thread {
                                if (!Python.isStarted()) Python.start(
                                    AndroidPlatform(context)
                                )
                                val py = Python.getInstance()
                                val ob = py.getModule("main") // Python Loading
                                var videoItems: String = ""
                                i.itemsList.forEach { m -> videoItems += m.videoId + "," }
                                val data =
                                    ob.callAttr("getPlaylistVideo", videoItems)
                                        .toString()

                                val json = JSONObject(data)
                                val fail_ = json.getJSONArray("fail")
                                var fail: List<Int> = emptyList()
                                for (i in 0..<fail_.length()) {
                                    fail += fail_.getInt(i)
                                }
                                var items: List<MediaSource> = emptyList()
                                for (i in 0..<json.getInt("length")) {
                                    if (i in fail) continue
                                    val m = json.getJSONObject(i.toString())
                                    val result = VideoLoader(
                                        "",
                                        context,
                                        color,
                                        false,
                                        m.toString()
                                    )
                                    result.mediaSource.updateMediaItem(
                                        result.mediaSource.mediaItem.buildUpon()
                                            .setTag(result).build()
                                    )
                                    items += result.mediaSource
                                }
                                hybridViewModel.setPlaylist(items)
                            }.start()
                        }
                    ) {
                        Text("Play")
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp, 0.dp, 5.dp, 0.dp),
                        shape = RoundedCornerShape(5.dp),
                        onClick = {
                            Thread {
                                try {
                                    val url = "https://www.youtube.com/playlist?list=${i.videoId}"
                                    val data = PlaylistLoader(url, context)
                                    if (data != null) {
                                        playListViewModel.insertPlayList(index,data)
                                        Log.i("PlaylistReload","Suc")
                                    }
                                } catch (e: Exception) {
                                    Log.e("PlaylistReload", e.toString())
                                }
                            }.start()
                        }
                    ) {
                        Text("Reload")
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .padding(0.dp, 0.dp, 5.dp, 0.dp),
                        shape = RoundedCornerShape(5.dp),
                        onClick = {
                            playListViewModel.removePlayList(index)
                        }
                    ) {
                        Text("Remove")
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 250.dp)
                ) {
                    items(i.itemsList) { l ->
                        var expand by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)){
                            Card(
                                modifier = Modifier
                                    .pointerInput(Unit){
                                        detectTapGestures(
                                            onLongPress = {
                                                expand=true
                                            },
                                            onTap = {
                                                hybridViewModel.setIsMini(false)
                                                hybridViewModel.setOpenVideo(true)
                                                hybridViewModel.setThumbnailUrl(l.thumbnailURL)
                                                hybridViewModel.setVideoId(l.videoId)
                                            }
                                        )
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(5.dp)
                                        .height(50.dp)
                                        .fillMaxWidth()
                                ) {
                                    AsyncImage(
                                        l.thumbnailURL, "item-thumbnail",
                                        modifier = Modifier
                                            .aspectRatio(16 / 9f)
                                            .fillMaxHeight()
                                    )
                                    Column(
                                        modifier = Modifier
                                            .padding(5.dp, 0.dp, 0.dp, 0.dp)
                                    ) {
                                        Text(
                                            l.title,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            l.channel,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            DropdownMenu(expand, onDismissRequest = {expand=false}) {
                                DropdownMenuItem(
                                    text = { Text("이어서 재생") },
                                    onClick = {
                                        expand=false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("마지막에 재생") },
                                    onClick = {
                                        expand=false
                                    }
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("플레이리스트에 추가") },
                                    onClick = {
                                        expand=false
                                    }
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}