package com.seon06.seonplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import androidx.media3.session.MediaSession
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.Manifest.permission

val NOTIFICATION_ID = 1
val PLAYBACK_CHANNEL_ID = "playback_channel" // 채널 ID 상수화


@SuppressLint("ObsoleteSdkInt")
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            PLAYBACK_CHANNEL_ID,
            "재생 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "ExoPlayer 재생 상태 알림"
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@SuppressLint("MissingPermission")
@OptIn(UnstableApi::class)
fun showNotification(context: Context, player: Player, mediaSession: MediaSession, data: VideoData) {
    createNotificationChannel(context) // 알림 채널 생성

    val notificationManager = NotificationManagerCompat.from(context)

    val builder = NotificationCompat.Builder(context, PLAYBACK_CHANNEL_ID).apply {
        setSmallIcon(android.R.drawable.ic_media_play)
        setContentTitle(data.title)
        setContentText(data.channel)
        setStyle(
            androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionCompatToken)
        )
        priority = NotificationCompat.PRIORITY_DEFAULT

        val intent = Intent(context, this::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        setContentIntent(pendingIntent)
        setOngoing(player.isPlaying)
        // Show controls on lock screen even when user disables private notifications.
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    notificationManager.notify(NOTIFICATION_ID, builder.build())
}


@SuppressLint("CoroutineCreationDuringComposition", "InvalidWakeLockTag", "WakelockTimeout",
    "InlinedApi"
)
@OptIn(UnstableApi::class)
@Composable
fun Viewer(id: String){
    Column {
        if (!Python.isStarted()) Python.start(AndroidPlatform(LocalContext.current))
        val py = Python.getInstance()
        val ob = py.getModule("main")
        val context = LocalContext.current
        val players = remember {
            ExoPlayer.Builder(context).apply {
                setLoadControl(DefaultLoadControl.Builder().apply {
                    setBufferDurationsMs(3000, 3000, 1500, 500)
                }.build())
                setRenderersFactory(DefaultRenderersFactory(context).apply {
                    setEnableDecoderFallback(true)
                })
            }.build()
        }

        val mediaSession = remember(players) { MediaSession.Builder(context,players).build() }

        var isloaded by remember { mutableStateOf(false) }

        var title by remember { mutableStateOf("") }
        var channel by remember { mutableStateOf("") }
        var verified by remember { mutableStateOf(false) }
        var subtitles by remember { mutableStateOf(emptyList<SubTitle>()) }
        var tags by remember { mutableStateOf(emptyList<String>()) }

        var index by remember { mutableStateOf(-1) }
        fun loaded(video: String, audio: String, audioType: String){
            if (!isloaded){
                Log.i("VIDEO", video)
                Log.i("AUDIO", audio)
                var hlsMediaSource: MediaSource? = null
                if (video.contains(".m3u8")){
                    Log.i("Video", "has M3U8")
                    hlsMediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(Uri.parse(video)))
                } else {
                    Log.i("Video", "has MP4")
                    hlsMediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(Uri.parse(video)))
                }
                val audioSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                    .createMediaSource(
                        MediaItem.Builder()
                            .setUri(Uri.parse(audio))
                            .setMimeType(audioType) // m4a 파일의 MIME 타입은 AUDIO_MPEG 또는 AUDIO_MP4
                            .build()
                    )

                players.addListener(object : Player.Listener {
                    /*@SuppressLint("WakelockTimeout", "Wakelock")
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            wakeLock.acquire()
                            Log.i("WAKE", "LOCK")
                        }
                        else {
                            wakeLock.release()
                            Log.i("WAKE", "RELEASE")
                        }
                    }*/
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState){
                            Player.STATE_READY -> {
                                Log.i("STATE", "READY")
                            }
                            Player.STATE_IDLE -> {
                                Log.i("STATE", "IDLE")
                            }
                            Player.STATE_BUFFERING -> {
                                Log.i("STATE", "BUFFERING")
                            }
                            Player.STATE_ENDED -> {
                                Log.i("STATE", "END")
                            }
                        }
                    }
                })

                players.setWakeMode(C.WAKE_MODE_NETWORK)
                players.setMediaSource(MergingMediaSource(hlsMediaSource, audioSource))
                players.prepare()
                players.playWhenReady = true
                isloaded = true
            }
        }

        LaunchedEffect(Unit) {
            if (!isloaded) while (true) {
                delay(1L)
                //index = findTargetLyric(subtitles, players.currentPosition)
            }
        }

        var hasNotificationPermission by remember { mutableStateOf(false) }

        // 권한 요청 런처
        val notificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            hasNotificationPermission = isGranted
        }

        SideEffect {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    hasNotificationPermission = true
                }
                shouldShowRequestPermissionRationale(context as Activity, permission.POST_NOTIFICATIONS) -> {
                    notificationPermissionLauncher.launch(permission.POST_NOTIFICATIONS)
                }
                else -> {
                    notificationPermissionLauncher.launch(permission.POST_NOTIFICATIONS)
                }
            }
        }

        LaunchedEffect(players.isPlaying) {
            if (players.isPlaying)
                //if (hasNotificationPermission) showNotification(context,players,mediaSession, VideoData(title,"",channel))
            else
                NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }

        val color_data = MaterialTheme.colorScheme.tertiaryContainer
        @SuppressLint("UnrememberedAnimatable")
        fun returnColor(): Animatable<Color, AnimationVector4D> {
            val color = Animatable(color_data)
            return color
        }

        GlobalScope.launch {
            if (!isloaded) {
                val data = ob.callAttr("get", id).toString()
                val json = JSONObject(data)
                Log.i("JSON", json.toString())
                val video = json.getString("video")
                val audio = json.getString("audio")
                val tagsJson = json.getJSONArray("tags")
                tags = emptyList<String>()
                for (i in 0..tagsJson.length() - 1) {
                    tags += tagsJson.getString(i)
                }
                val audioTypeData = json.getString("audio_type")
                val audioType =
                    if (audioTypeData.equals("webm")) MimeTypes.AUDIO_WEBM else MimeTypes.AUDIO_MPEG
                Log.i("Get AudioType", audioTypeData)
                Log.i("Gen AudioType", audioType)
                withContext(Dispatchers.Main) {
                    loaded(video, audio, audioType)
                }
                title = json.getString("title")
                channel = json.getString("channel")
                verified = json.getBoolean("verified")
                val _subtitles = json.getJSONObject("subtitles")
                try {
                    val url = URL(_subtitles.getString("ko"))
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val inputStream = connection.inputStream
                    val text = inputStream.bufferedReader().use { it.readText() }
                    //subtitles = genSubtitle(JSONObject(text).getJSONArray("events"), Color = ::returnColor)
                    Log.i("@@@@@@@@@", "!")
                } catch (e: Exception) {
                    Log.e("SUB_ERR", e.toString())
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                mediaSession.release()
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
        Text(title)
        LazyRow {
            items(tags){ tag ->
                Button(onClick = {}) {
                    Text(tag)
                }
            }
        }
        Row {
            Text(channel)
            if (verified) Icon(Icons.Filled.CheckCircle,"verified")
        }

        //SubtitleView(subtitles, index)
    }
}