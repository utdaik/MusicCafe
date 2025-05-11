package com.example.cafemusicchange.ui.home


import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cafemusicchange.MainViewModel
import com.example.cafemusicchange.api.JamendoApiService
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.repository.JamendoRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel,
    mainViewModel: MainViewModel
) {
    val morning by viewModel.moring.collectAsState()
    val vintage by viewModel.vintage.collectAsState()
    val indie by viewModel.indie.collectAsState()
    val jazz by viewModel.jazz.collectAsState()
    val classic by viewModel.classic.collectAsState()
    val lofi by viewModel.lofi.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Khám phá",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Đảm bảo nội dung không bị che bởi TopBar
                .padding(horizontal = 16.dp) // Thêm padding ngang để tránh tràn viền
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    LazyRowSection("Morning Vibes", morning, navController)
                }
                item {
                    LazyRowSection("Acoustic & Indie", indie, navController)
                }
                item {
                    LazyRowSection("Vintage & Retro", vintage, navController)
                }
                item {
                    LazyRowSection("Jazz & Blues", jazz, navController)
                }
                item {
                    LazyRowSection("Classical & Instrumental", classic, navController)
                }
                item {
                    LazyRowSection("Lofi & Chill", lofi, navController)
                }
            }
        }
    }
}


@Composable
fun LazyRowSection(title: String, tracks: List<JamendoTrack>, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyRow( // 🔥 Vẫn giữ LazyRow nhưng truyền toàn bộ danh sách tracks
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tracks.chunked(3)) { trackGroup -> // 🔥 Vẫn giữ nhóm 3 bài hát mỗi cột
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    trackGroup.forEach { track ->
                        TrackItem(track, onTrackClick = {
                            val tracksJson = Uri.encode(Gson().toJson(tracks)) // 🔥 Toàn bộ danh sách tracks
                            val trackIndex = tracks.indexOfFirst { it.id == track.id } // 🔥 Vị trí bài hát trong toàn bộ danh sách
                            navController.navigate("player/$tracksJson/$trackIndex")
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}




@Composable
fun TrackItem(track: JamendoTrack, onTrackClick: (JamendoTrack) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onTrackClick(track)
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

    }
}









