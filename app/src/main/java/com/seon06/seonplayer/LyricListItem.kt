package com.seon06.seonplayer

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.ui.graphics.Color

data class LyricListItem (
    val lang: String,
    val lyrics: List<LyricItem>
)

data class LyricItem (
    val startMS: Int,
    val durationMS: Int,
    val text: String,
    val color: Animatable<Color, AnimationVector4D>,
)

fun getLyric(lang: String, data: List<LyricListItem>): LyricListItem{
    data.forEach { item ->
        if (item.lang == lang) {
            Log.i("getLyric", "Find lang :  "+ lang + " != " + item.lang)
            return item
        }
        Log.i("getLyric", lang + " != " + item.lang)
    }
    return LyricListItem(lang, emptyList())
}