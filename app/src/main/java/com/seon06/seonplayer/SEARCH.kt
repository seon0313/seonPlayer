package com.seon06.seonplayer

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KFunction1

class SearchViewModel : ViewModel() {
    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _suggests = MutableStateFlow(emptyList<String>())
    val suggests = _suggests.asStateFlow()

    fun init() {
        _suggests.value = emptyList()
    }

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()
    fun onSearchTextChange(text: String) {
        _searchText.value = text
        Thread {
            _suggests.value = getSuggest(text)
        }.start()

    }
    fun onToogleSearch() {
        _isSearching.value = !_isSearching.value
        if (!_isSearching.value) {
            onSearchTextChange("")
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(
    hybridViewModel: HybridViewModel
) {
    val searchViewModel by remember { mutableStateOf(SearchViewModel()) }
    val searchText by searchViewModel.searchText.collectAsState()
    val isSearching by searchViewModel.isSearching.collectAsState()
    val suggests by searchViewModel.suggests.collectAsState()

    var searchItems by remember { mutableStateOf(emptyList<JSONObject>()) }
    val context = LocalContext.current

    fun getSuggest_cmd(text: String){
        searchViewModel.onToogleSearch()
        searchViewModel.onSearchTextChange(text)

        Thread{
            var lists = emptyList<JSONObject>()


            if (!Python.isStarted()) Python.start(AndroidPlatform(context))
            val py = Python.getInstance()
            val ob = py.getModule("main")
            val data = ob.callAttr("run", text, 25).toString()
            val json = JSONArray(data)
            for (i in 0 .. json.length()-1) {
                lists += json.getJSONObject(i)
            }
            searchItems = lists

        }.start()
    }
    Box(modifier = Modifier.fillMaxSize().zIndex(if (hybridViewModel.openVideo.value) -1f else 1f)) {
        Column {
            SearchBar(
                query = searchText,
                placeholder = { Text(stringResource(R.string.search)) },
                onQueryChange = searchViewModel::onSearchTextChange,
                onSearch = { getSuggest_cmd(searchText) },
                active = isSearching,
                onActiveChange = { searchViewModel.onToogleSearch() },
                modifier = Modifier
                    .fillMaxWidth(),
                windowInsets = WindowInsets(top = 0.dp)
            ) {
                LazyColumn {
                    items(suggests) { text ->
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(),
                            border = BorderStroke(0.dp, Color.Transparent),
                            modifier = Modifier.background(Color.Transparent),
                            onClick = { getSuggest_cmd(text) }
                        ) {
                            Text(text = text, fontSize = 20.sp, fontFamily = MaterialTheme.typography.bodySmall.fontFamily)
                        }
                    }
                }


            }

            LazyColumn {
                itemsIndexed(searchItems) { index, i ->
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(),
                        border = BorderStroke(0.dp, Color.Transparent),
                        modifier = Modifier.background(Color.Transparent),
                        onClick = {
                            hybridViewModel.setIsMini(false)
                            hybridViewModel.setOpenVideo(true)
                            hybridViewModel.setVideoId(i.getString("id"))
                            //nav.navigate(navigationItems.viewer.route+"/"+i.getString("id"))
                        },
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Column {
                            val url = i.getString("thumbnail").toString()
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                i.getString("title"),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 2
                            )
                            var l = emptyList<String>()
                            for (_i in 0..i.getJSONArray("tags").length() - 1) {
                                l += i.getJSONArray("tags").getString(_i)
                            }
                            Row {
                                Text(i.getString("channel"))
                                LazyRow {
                                    items(l) { tag ->
                                        Button(onClick = {}) {
                                            Text(tag)
                                        }
                                    }
                                }
                            }

                        }
                    }
                    if (hybridViewModel.isMini.value && searchItems.lastIndex == index) Spacer(modifier = Modifier.height(80.dp))
                }

            }
        }
    }
}