package com.app.simpleplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.app.simpleplayer.domain.models.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MusicManager(private val context: Context) : ContentResolver(context) {

    fun getExternalContent(): Flow<List<Song>> = flow {
        val songs = mutableListOf<Song>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        val selection = MediaStore.Audio.Media.IS_MUSIC

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION
        )

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} ASC"

        context.contentResolver
            .query(
                collection,
                projection,
                selection,
                null,
                sortOrder,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateAdded = cursor.getInt(dateAddedColumn)
                    val uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val title = cursor.getString(titleColumn)
                    val duration = cursor.getInt(durationColumn)
                    songs.add(Song(id, title, dateAdded, uri, duration.toFloat()))
                }

                emit(songs)
            } ?: emit(listOf())
    }
}
