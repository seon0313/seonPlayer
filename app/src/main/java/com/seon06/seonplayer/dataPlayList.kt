package com.seon06.seonplayer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

object PlayListSerializer : Serializer<PlaylistList> {
    override val defaultValue: PlaylistList
        get() = PlaylistList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PlaylistList {
        try {
            return PlaylistList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException){
            throw CorruptionException("Cant read proto", e)
        }
    }

    override suspend fun writeTo(t: PlaylistList, output: OutputStream) {
        t.writeTo(output)
    }
}

class PlayListRepository(private val dataStore: DataStore<PlaylistList>) {
    val flow: Flow<PlaylistList> = dataStore.data

    suspend fun addPlaylist(playListData: PlaylistData){
        dataStore.updateData { data ->
            data
                .toBuilder()
                .addItems(playListData)
                .build()
        }
    }

    suspend fun insertPlayList(index: Int, playListItem: PlaylistData) {
        dataStore.updateData { data ->
            data
                .toBuilder()
                .setItems(index, playListItem)
                .build()
        }
    }
    suspend fun removePlayList(index: Int) {
        dataStore.updateData { data ->
            data
                .toBuilder()
                .removeItems(index)
                .build()
        }
    }
}

class PlayListViewModel(private val repository: PlayListRepository) : ViewModel() {
    val flow: Flow<PlaylistList> = repository.flow

    fun addPlayList(playListItem: PlaylistData){
        viewModelScope.launch { repository.addPlaylist(playListItem) }
    }

    fun insertPlayList(index: Int, playListItem: PlaylistData){
        viewModelScope.launch { repository.insertPlayList(index,playListItem) }
    }

    fun removePlayList(index: Int){
        viewModelScope.launch { repository.removePlayList(index) }
    }
}

class PlayListViewModelFactory(private val repository: PlayListRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(PlayListViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return PlayListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}