import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cafemusicchange.ui.playback.MusicPlayer

@Composable
fun MiniPlayer(viewModel: MusicPlayer, onExpand: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val track by viewModel.tracks.collectAsState()

    Card(
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(8.dp)
            .clickable { onExpand() } // Nhấn vào Mini Player để mở Full Player
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh bìa bài hát
            Image(
                painter = rememberAsyncImagePainter(track?.audioUrl ?: ""),
                contentDescription = "Album Cover",
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.small)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Tiêu đề bài hát
            Text(
                text = track?.title ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            // Nút Play/Pause
            IconButton(onClick = { viewModel.playPause() }) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause"
                )
            }
        }
    }
}




