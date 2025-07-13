package com.seon06.seonplayer

import android.content.Context
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HybridViewModel : ViewModel() {
    val openVideo = mutableStateOf(false)

    val isMini = mutableStateOf(false)

    private val _videoId = MutableStateFlow("")
    val videoId = _videoId.asStateFlow()

    val bottomBarHeight = mutableStateOf(0)

    fun setOpenVideo(data: Boolean) {
        openVideo.value = data
    }
    fun setIsMini(data: Boolean) {
        isMini.value = data
    }
    fun setVideoId(data: String) {
        _videoId.value = data
    }
    fun setBottomBarHeight(data: Int) {
        bottomBarHeight.value = data
    }

    private val _mediaSource = MutableLiveData<MediaSource>()
    val mediaSource: LiveData<MediaSource> = _mediaSource

    fun setMediaSource(mediaSource: MediaSource) {
        _mediaSource.value = mediaSource
    }

    val playlist: MutableState<List<MediaSource>> = mutableStateOf(emptyList())
    fun setPlaylist(data: List<MediaSource>){
        playlist.value = data
    }


    val isloaded = mutableStateOf(false)
    val index = mutableStateOf(-1)
    val subtitles: MutableState<List<LyricListItem>> = mutableStateOf(emptyList())
    val subtitle: MutableState<List<LyricItem>> = mutableStateOf(emptyList())
    val videoData: MutableState<VideoData> = mutableStateOf(VideoData("","","",false,""))
    val tagItem: MutableState<List<TagItem>> = mutableStateOf(emptyList())
    val lang: MutableState<String> = mutableStateOf("ko")
    val thumbnailUrl: MutableState<String> = mutableStateOf("ko")

    fun setIsloaded(data: Boolean) {isloaded.value = data}
    fun setIndex(data: Int) {index.value = data}
    fun setSubtitles(data: List<LyricListItem>) {subtitles.value = data}
    fun setSubtitle(data: List<LyricItem>) {subtitle.value = data}
    fun setVideoData(data: VideoData) {videoData.value = data}
    fun setTagItem(data: List<TagItem>) {tagItem.value = data}
    fun setLang(data: String) {lang.value = data}
    fun setThumbnailUrl(data: String) {thumbnailUrl.value = data}
}