package com.example.cafemusicchange.ui.playlist

import MoreOptionsBottomSheet
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.local.PlaylistSong
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    navController: NavHostController,
    viewModel: PlaylistViewModel = hiltViewModel(),
    playlistId: Long,
    userId: Long
) {
    val playlistSongs by viewModel.playlistSongs.collectAsState()
    var selectedSong by remember { mutableStateOf<JamendoTrack?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Khi màn hình mở, tự động load danh sách bài hát trong playlist
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistSongs(playlistId, userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết Playlist", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) },
//                actions = {
//                    IconButton(onClick = { /* Mở menu */ }) {
//                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
//                    }
//                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn {
                items(playlistSongs) { song ->
                    SongItem(song, onTrackClick = {
                        val tracksJson = Uri.encode(Gson().toJson(playlistSongs))
                        val trackIndex = playlistSongs.indexOfFirst { it.id == song.id }
                        navController.navigate("player/$tracksJson/$trackIndex")

                    },
                        onMoreOptionsClick = {
                            selectedSong = song
                            showBottomSheet = true
                        })
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // Nút thêm bài hát vào playlist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("search") }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm bài hát", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }

        }
    }
    if (showBottomSheet && selectedSong != null) {
        MoreOptionsBottomSheet(
            track = selectedSong!!,
            onDismiss = { showBottomSheet = false },
            onRemoveFromPlaylist = {
                viewModel.removeSongFromPlaylist(playlistId, selectedSong!!.id, userId)
                showBottomSheet = false
            }
        )
    }
}


@Composable
fun SongItem(track: JamendoTrack, onTrackClick: (Long) -> Unit,onMoreOptionsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onTrackClick(track.id)
            }
    ) {
        Image(
            painter = rememberAsyncImagePainter(track.albumImage),
            contentDescription = "Ảnh bài hát",
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = track.title, style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            )
            Text(text = track.artist, style = TextStyle(
                fontSize = 16.sp
            )
            )
        }
        IconButton(onClick = onMoreOptionsClick) {
            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    track: JamendoTrack,
    onDismiss: () -> Unit,
    onRemoveFromPlaylist: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tùy chọn bài hát", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(track.title, fontSize = 18.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onRemoveFromPlaylist) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Xóa bài hát")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xóa khỏi Playlist")
            }
        }
    }
}

