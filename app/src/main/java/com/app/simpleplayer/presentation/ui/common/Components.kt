package com.app.simpleplayer.presentation.ui.common

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.simpleplayer.domain.models.Song

@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    album: String,
    song: Song,
    onCLick: (Song) -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = { onCLick(song) })
            .padding(12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(44.dp),
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            Text(
                text = album,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun SongItemPreview() {
    SongItem(
        album = "Nirvana - Smells like teen spirit",
        song = Song(0, "Nirvana - Smells like teen spirit", 0, Uri.EMPTY, 0.0f),
        onCLick = {
        }
    )
}
