package com.example.cafemusicchange.ui.library

import MoreOptionsBottomSheet
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.local.DownloadedSong
import com.example.cafemusicchange.ui.playback.MusicPlayer
import com.example.cafemusicchange.ui.playlist.PlaylistViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    navController: NavHostController,
    playlistViewModel: PlaylistViewModel = hiltViewModel(),
) {
    val downloads by playlistViewModel.downloadedTracks.collectAsState()

    var selected by remember { mutableStateOf<DownloadedSong?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        playlistViewModel.loadDownloads()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (downloads.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có bài nhạc nào tải về")
                }
            } else {
                LazyColumn {
                    items(downloads) { song ->
                        DownloadItem(
                            song = song,
                            onPlay = {
                                // 1) map DownloadedSong → JamendoTrack
                                val jamTracks = downloads.map { ds ->
                                    JamendoTrack(
                                        id = ds.songId,
                                        title = ds.title,
                                        artist = ds.artist,
                                        albumImage = ds.albumImage,
                                        audioUrl = ds.filePath  // ExoPlayer sẽ chơi file local
                                    )
                                }
                                // 2) serialize & encode
                                val json = Uri.encode(Gson().toJson(jamTracks))
                                // 3) tìm index
                                val idx = jamTracks.indexOfFirst { it.id == song.songId }
                                // 4) navigate
                                navController.navigate("player/$json/$idx")
                            },
                            onMore = {
                                selected = song
                                showSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSheet && selected != null) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Tùy chọn", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(selected!!.title)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = {
                    playlistViewModel.toggleDownloaded(
                        track = JamendoTrack( // bạn có thể chuyển DownloadedSong -> JamendoTrack tạm
                            id = selected!!.songId,
                            title = selected!!.title,
                            artist = selected!!.artist,
                            albumImage = selected!!.albumImage,
                            audioUrl = null.toString()
                        ),
                        filePath = selected!!.filePath
                    )
                    showSheet = false
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Xóa khỏi Offline", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun DownloadItem(
    song: DownloadedSong,
    onPlay: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(song.albumImage),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(song.title, fontSize = 18.sp)
            Text(song.artist, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
        IconButton(onClick = onMore) {
            Icon(Icons.Filled.MoreVert, contentDescription = "More")
        }
    }
}
