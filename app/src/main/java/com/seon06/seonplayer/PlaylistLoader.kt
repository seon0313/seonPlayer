package com.seon06.seonplayer

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject

fun PlaylistLoader (playlist_id: String, context: Context): PlaylistData? {
    if (!Python.isStarted()) Python.start(AndroidPlatform(context))
    val py = Python.getInstance()
    val ob = py.getModule("main") // Python Loading

    val data = ob.callAttr("getPlaylist", playlist_id).toString() // Get Python Code Result
    val json = JSONObject(data)

    val title = json.getString("title")
    val id = json.getString("id")
    val uploader = json.getString("uploader")
    val description = json.getString("description")

    val result = PlaylistData.newBuilder()
        .setTitle(title)
        .setUploader(uploader)
        .setVideoId(id)
        .setDescription(description)

    val items = json.getJSONArray("items")
    for (i in 0..<items.length()){
        val item = items.getJSONObject(i)

        result.addItems(
            PlaylistItem.newBuilder()
                .setTitle(item.getString("title"))
                .setChannel(item.getString("channel"))
                .setVideoId(item.getString("id"))
                .setThumbnailURL(item.getString("thumbnail"))
                .build()
        )
    }
    return result.build()
}