package com.app.simpleplayer.presentation.ui.common

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.simpleplayer.R
import com.app.simpleplayer.domain.models.Song

@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    album: String,
    painter: Painter?,
    song: Song,
    onCLick: (Song) -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onCLick(song) }
            .padding(12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (painter == null) {
            SongItemPlaceholder()
        } else {
            Image(painter = painter, contentDescription = null)
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Column {
            Text(
                text = album,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SongItemPlaceholder() {
    Surface(
        modifier = Modifier.size(44.dp),
        color = Color.LightGray,
        shape = RoundedCornerShape(5.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_music_note_24),
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Preview
@Composable
fun SongItemPreview() {
    val context = LocalContext.current
    SongItem(
        album = "Nirvana - Smells like teen spirit",
        painter = null,
        song = Song(0, "Nirvana - Smells like teen spirit", 0, Uri.EMPTY),
        onCLick = { clickedSong ->
            Toast.makeText(context, clickedSong.title, Toast.LENGTH_LONG).show()
        }
    )
}
