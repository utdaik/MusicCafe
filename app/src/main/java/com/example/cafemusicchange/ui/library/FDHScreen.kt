package com.example.cafemusicchange.ui.library

import MoreOptionsBottomSheet
import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.ui.playlist.DEFAULT_PLAYLIST_IMAGE
import com.example.cafemusicchange.ui.playlist.PlaylistViewModel
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    navController: NavHostController,
    viewModel: PlaylistViewModel = hiltViewModel(),
    userId: Long
) {
    // 1. State của danh sách favorite
    val favorites by viewModel.favoriteTracks.collectAsState()

    // 2. State để ghi track đang chọn để hiển thị BottomSheet
    var selectedTrack by remember { mutableStateOf<JamendoTrack?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // 3. Khi vào màn hình, load các bài yêu thích
    LaunchedEffect(userId) {
        viewModel.loadFavoriteTracks(userId)
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yêu thích", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Quay lại")
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
            if (favorites.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bạn chưa có bài hát yêu thích nào")
                }
            } else {
                LazyColumn {
                    items(favorites) { track ->
                        FavoriteSongItem(
                            track = track,
                            onPlay = {
                                val json = Uri.encode(Gson().toJson(favorites))
                                val idx = favorites.indexOfFirst { it.id == track.id }
                                navController.navigate("player/$json/$idx")
                            },
                            onMore = {
                                selectedTrack = track
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    // 4. Bottom sheet “More options” để xoá khỏi yêu thích
    if (showBottomSheet && selectedTrack != null) {
        MoreOptionsBottomSheet(
            track = selectedTrack!!,
            onDismiss = { showBottomSheet = false },
            onRemove = {
                viewModel.toggleFavorite(selectedTrack!!.id, userId)
                showBottomSheet = false
            }
        )
    }
}

@Composable
private fun FavoriteSongItem(
    track: JamendoTrack,
    onPlay: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(track.albumImage ?: DEFAULT_PLAYLIST_IMAGE),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(track.artist, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
        IconButton(onClick = onMore) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Tuỳ chọn")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreOptionsBottomSheet(
    track: JamendoTrack,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text("Tuỳ chọn bài hát", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(track.title, fontSize = 18.sp)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text("Xoá khỏi Yêu thích", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
