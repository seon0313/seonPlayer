package com.seon06.seonplayer

import android.Manifest
import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(UnstableApi::class)
@Composable
fun MediaNotification(players: ExoPlayer, videoData: VideoData) {
    val NOTIFICATION_ID = 1
    val PLAYBACK_CHANNEL_ID = "playback_channel" // 채널 ID 상수화

    val context = LocalContext.current

    @SuppressLint("ObsoleteSdkInt")
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                "재생 알림",
                NotificationManager.IMPORTANCE_LOW
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
    fun showNotification(
        context: Context,
        player: Player,
        mediaSession: MediaSession,
        data: VideoData
    ) {
        createNotificationChannel(context) // 알림 채널 생성

        val notificationManager = NotificationManagerCompat.from(context)

        val builder = NotificationCompat.Builder(context, PLAYBACK_CHANNEL_ID).apply {
            setSmallIcon(android.R.drawable.ic_media_play)
            setContentTitle(data.title)
            setContentText(data.channel)
            Thread{
                val bitmap = getBitmapFromURL(data.thumbnailURL)
                if (bitmap != null){
                    setLargeIcon(bitmap)
                }
            }.start()

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

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
    val mediaSession = remember(players) { MediaSession.Builder(context,players).build() }

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

    DisposableEffect(Unit) {
        onDispose {
            mediaSession.release()
        }
    }

    LaunchedEffect(videoData) {
        Log.i("MEDIA_NOTIFICATION","!!!!!!!!!!!!!!!!")
        if (!players.isReleased && players.mediaItemCount>0)
            if (hasNotificationPermission) showNotification(context,players,mediaSession, videoData)
        else
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}

fun getBitmapFromURL(src: String): Bitmap? {
    return try {
        val url = URL(src)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input = connection.inputStream
        BitmapFactory.decodeStream(input)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}