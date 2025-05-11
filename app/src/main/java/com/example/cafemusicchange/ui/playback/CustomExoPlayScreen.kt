
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cafemusicchange.MainViewModel
import com.example.cafemusicchange.local.Playlist
import com.example.cafemusicchange.ui.playback.MusicPlayer
import com.example.cafemusicchange.ui.playback.PlaybackState
import com.example.cafemusicchange.ui.playlist.DEFAULT_PLAYLIST_IMAGE
import com.example.cafemusicchange.ui.playlist.PlaylistViewModel

@Composable
fun CustomExoPlayerScreen(
    viewModel: MusicPlayer = hiltViewModel(),
    navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val trackState by viewModel.tracks.collectAsState()
    val mainState = mainViewModel.uiState.collectAsState()
    var userId = mainState.value.userId

    val showBottomSheet = remember { mutableStateOf(false) }
    val showPlaylistSheet = remember { mutableStateOf(false) }

    // Đảm bảo vị trí hiện tại và thời lượng bài hát hợp lệ
    val safeCurrentPosition = uiState.currentPosition.coerceAtLeast(0L)
    val safeDuration by remember {
        derivedStateOf { if (uiState.duration > 0L) uiState.duration else 1L }
    }

    // State của slider
    var sliderPosition by remember { mutableStateOf(safeCurrentPosition.toFloat()) }
    LaunchedEffect(safeCurrentPosition) {
        sliderPosition = safeCurrentPosition.toFloat()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TopBar(navHostController, showBottomSheet)
        Spacer(modifier = Modifier.height(80.dp))
        AlbumCover(imageUrl = trackState?.albumImage)
        Spacer(modifier = Modifier.height(20.dp))
        // Hiển thị tiêu đề và nghệ sĩ
        Text(
            text = trackState?.title ?: "Unknown Title",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = trackState?.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(30.dp))
        Row {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        // Thanh slider và thời gian
        PlaybackSlider(
            sliderPosition = sliderPosition,
            duration = safeDuration,
            onValueChange = { newValue -> sliderPosition = newValue },
            onValueChangeFinished = { viewModel.seekTo(sliderPosition.toLong()) },
            currentTime = safeCurrentPosition
        )
        Spacer(modifier = Modifier.height(16.dp))
        MusicPlayerControls(viewModel, uiState)
    }

    if (showBottomSheet.value) {
        MoreOptionsBottomSheet(
            onDismiss = { showBottomSheet.value = false },
            onAddToPlaylist = {
                showBottomSheet.value = false  // 🔥 Đảm bảo đã đóng
                showPlaylistSheet.value = true // 🔥 Mở PlaylistSelectionSheet
            }
        )
    }

    Log.i("Check user id in play", userId.toString())

    if (showPlaylistSheet.value && userId != null) {
        PlaylistSelectionSheet(
            userId = userId,
            playlistViewModel = hiltViewModel(),
            onDismiss = { showPlaylistSheet.value = false },
            onPlaylistSelected = { playlist ->
                viewModel.tracks.value?.id?.let { trackId ->
                    viewModel.addSongToPlaylist(playlist.playlistId, trackId, userId)
                    mainViewModel.setNotified("Thêm thành công")
                }
                showPlaylistSheet.value = false
            }
        )
    }


}

@Composable
fun TopBar(navController: NavHostController,showBottomSheet: MutableState<Boolean>) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = "Collapse",
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(onClick = { showBottomSheet.value = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun AlbumCover(imageUrl: String?) {
    Image(
        painter = rememberAsyncImagePainter(imageUrl),
        contentDescription = "Album Cover",
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(250.dp)
    )
}

@Composable
fun PlaybackSlider(
    sliderPosition: Float,
    duration: Long,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    currentTime: Long
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Slider(
            value = sliderPosition.coerceIn(0f, duration.toFloat()),
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = 0f..duration.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentTime), style = MaterialTheme.typography.labelMedium)
            Text(text = formatTime(duration), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun MusicPlayerControls(viewModel: MusicPlayer, uiState: PlaybackState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
    ) {
        IconButton(onClick = { viewModel.toggleShuffle() }) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = "Shuffle",
                modifier = Modifier.size(48.dp),
                tint = if (uiState.isShuffle) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
        IconButton(onClick = { viewModel.playPrevious() }) {
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(48.dp)
            )
        }
        IconButton(
            onClick = { viewModel.playPause() },
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = if (uiState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause",
                modifier = Modifier.size(48.dp)
            )
        }
        IconButton(onClick = { viewModel.playNext() }) {
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(48.dp)
            )
        }
        IconButton(onClick = { viewModel.toggleRepeat() }) {
            Icon(
                imageVector = Icons.Filled.Repeat,
                contentDescription = "Repeat",
                modifier = Modifier.size(48.dp),
                tint = if (uiState.isRepeat) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

fun formatTime(timeMs: Long): String {
    val minutes = (timeMs / 1000) / 60
    val seconds = (timeMs / 1000) % 60
    return "%02d:%02d".format(minutes, seconds)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    onDismiss: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    var shouldOpenPlaylist by remember { mutableStateOf(false) }
    val shouldOpenPlaylistState = rememberUpdatedState(shouldOpenPlaylist)

    ModalBottomSheet(
        onDismissRequest = {
            shouldOpenPlaylist = true // 🔥 Đánh dấu rằng sẽ mở danh sách Playlist sau khi đóng
            onDismiss() // 🔥 Đóng BottomSheet đầu tiên
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tùy chọn bài hát", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { shouldOpenPlaylist = true }) {
                Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = "Add to Playlist")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm vào Playlist")
            }
        }
    }

    // 🔥 Chỉ mở PlaylistSelectionSheet sau khi BottomSheet đầu tiên thực sự đóng
    LaunchedEffect(shouldOpenPlaylistState.value) {
        if (shouldOpenPlaylistState.value) {
            shouldOpenPlaylist = false
            onAddToPlaylist()
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSelectionSheet(
    userId: Long,
    playlistViewModel: PlaylistViewModel,
    onDismiss: () -> Unit,
    onPlaylistSelected: (Playlist) -> Unit
) {
    val playlists by playlistViewModel.userPlaylists.collectAsState()

    LaunchedEffect(userId) {
        playlistViewModel.loadUserPlaylists(userId)
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Chọn Playlist", style = MaterialTheme.typography.titleMedium)

            LazyColumn {
                items(playlists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = { onPlaylistSelected(playlist) }
                    )
                }
            }
        }
    }
}


@Composable
fun PlaylistItem(playlist: Playlist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ảnh đại diện của Playlist (nếu có)
        Image(
            painter = rememberAsyncImagePainter(playlist.albumImage ?: DEFAULT_PLAYLIST_IMAGE),
            contentDescription = "Playlist Image",
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Hiển thị tên Playlist
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}


