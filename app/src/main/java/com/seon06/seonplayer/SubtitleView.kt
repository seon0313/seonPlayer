package com.seon06.seonplayer

import android.annotation.SuppressLint
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SubtitleView(data: List<LyricItem>, Index: Int){
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val defaultColor = MaterialTheme.colorScheme.tertiaryContainer

    if (data.isNotEmpty()){
        val color = MaterialTheme.colorScheme.tertiary
        LaunchedEffect(Index) {
            if (Index != -1) {
                coroutineScope.launch {
                    listState.animateScrollToItem(Index)
                    data[Index].color.animateTo(color,animationSpec = tween(durationMillis = 500))
                }
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(data) { index, item ->
            if (index!=Index){
                if (data[index].color.value != defaultColor) {
                    coroutineScope.launch {
                        data[index].color.animateTo(defaultColor)
                    }
                }
            }
            Text(
                item.text, fontSize = 30.sp, color = item.color.value,
                modifier = Modifier
                    .padding(35.dp, 20.dp)
                    .heightIn(max = 150.dp)
            )
        }
    }
}

fun findTargetLyric(data: List<LyricItem>, pos: Long): Int{
    if (data.isEmpty()) return -1
    for (i in data.indices) {
        if (data[i].startMS <= pos && pos <= data[i].startMS+data[i].durationMS) {
            return i
        }
    }
    return -1
}