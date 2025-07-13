package com.seon06.seonplayer

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.ui.graphics.Color
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


data class returnVideoLoader(
    val videoData: VideoData,
    val mediaSource: MediaSource,
    val lyricListItem: List<LyricListItem>,
    val tagItem: List<TagItem>,
)

@OptIn(UnstableApi::class)
@SuppressLint("MutableCollectionMutableState")
fun VideoLoader(video_id: String, context: Context, tertiaryContainer: Color, load: Boolean=true, insertData: String=""): returnVideoLoader {
    @SuppressLint("UnrememberedAnimatable")
    fun returnColor(): Animatable<Color, AnimationVector4D> {
        val color = Animatable(tertiaryContainer)
        return color
    }
    var data = ""
    if (load) {
        if (!Python.isStarted()) Python.start(AndroidPlatform(context))
        val py = Python.getInstance()
        val ob = py.getModule("main") // Python Loading

        data = ob.callAttr("get", video_id).toString() // Get Python Code Result
    } else data = insertData
    val json = JSONObject(data)
    Log.i("JSON", json.toString())
    val video = json.getString("video")
    val audio = json.getString("audio")

    val audioTypeData = json.getString("audio_type") // get audio type
    val audioType =
        if (audioTypeData.equals("webm")) MimeTypes.AUDIO_WEBM else MimeTypes.AUDIO_MPEG
    Log.i("Get AudioType", audioTypeData)
    Log.i("Gen AudioType", audioType)


    var hlsMediaSource: MediaSource? = null // Video Load
    if (video.contains(".m3u8")){
        Log.i("Video", "has M3U8")
        hlsMediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(Uri.parse(video)))
    } else {
        Log.i("Video", "has MP4")
        hlsMediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(Uri.parse(video)))
    }
    var audioSource: MediaSource? = null
    if (audio.contains(".m3u8")){
        audioSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory()) // Audio Load
            .createMediaSource(
                MediaItem.Builder()
                    .setUri(Uri.parse(audio))
                    .setMimeType(audioType)
                    .build()
            )
    } else {
        audioSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory()) // Audio Load
            .createMediaSource(
                MediaItem.Builder()
                    .setUri(Uri.parse(audio))
                    .setMimeType(audioType) // m4a 파일의 MIME 타입은 AUDIO_MPEG 또는 AUDIO_MP4
                    .build()
            )
    }


    val mediaSource = MergingMediaSource(hlsMediaSource, audioSource) // Marging Video and Audio Source

    val videoData = VideoData(
        title = json.getString("title"),
        id = video_id,
        channel = json.getString("channel"),
        verified = json.getBoolean("verified"),
        thumbnailURL = json.getString("thumbnail"),
        description = json.getString("description")
    )

    val tags = mutableListOf<TagItem>()
    val tagsJson = json.getJSONArray("tags") // Get Video Tags
    for (i in 0..<tagsJson.length()) {
        tags.add(TagItem(name=tagsJson.getString(i)))
    }

    var subtitles = emptyList<LyricListItem>()
    val _subtitles = json.getJSONObject("subtitles")
    try {
        val langs = _subtitles.keys()
        for (i in langs.iterator()){
            try {
                val url = URL(_subtitles.getString(i))
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val inputStream = connection.inputStream
                val text = inputStream.bufferedReader().use { it.readText() }
                subtitles += LyricListItem(lang=i, lyrics=genSubtitle(JSONObject(text).getJSONArray("events"), Color = ::returnColor))
            } catch (e: Exception){
                Log.i("Get "+i, e.toString())
            }
        }
        Log.i("SUB_LOAD", "SUC")
    } catch (e: Exception) {
        Log.e("SUB_ERR", e.toString())
    }

    return returnVideoLoader(videoData, mediaSource, subtitles, tags)
}