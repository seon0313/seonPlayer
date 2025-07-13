package com.seon06.seonplayer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONArray

data class VideoData (
    val title: String,
    val id: String,
    val channel: String,
    val verified: Boolean = false,
    var thumbnailURL: String = "",
    val description: String = ""
)

data class TagItem (
    val name: String
)

data class SubTitle (
    val startMS: Int,
    val durationMS: Int,
    val text: String,
    val color: Animatable<Color, AnimationVector4D>,
)

data class Video(
    val videoData: VideoData,
    val subtitles: MutableState<List<LyricListItem>>,
    val tagItem: MutableState<List<TagItem>> = mutableStateOf(emptyList()),
)

fun genSubtitle(data: JSONArray, Color:() -> Animatable<Color, AnimationVector4D>): List<LyricItem>{
    var list = emptyList<LyricItem>()
    for (i in 0..data.length()-1){
        val item = data.getJSONObject(i)
        var text = ""
        val textData = item.getJSONArray("segs")
        for (l in 0..textData.length()-1){
            val textItem = textData.getJSONObject(l)
            if (textItem.has("utf8")){
                text = textItem.getString("utf8")
            }
        }
        list += LyricItem(
            startMS = item.getInt("tStartMs"),
            durationMS = item.getInt("dDurationMs"),
            text = text,
            color = Color()
        )
    }
    return list
}