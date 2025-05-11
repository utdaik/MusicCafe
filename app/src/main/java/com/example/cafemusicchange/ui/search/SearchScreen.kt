package com.example.cafemusicchange.ui.search

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cafemusicchange.MainViewModel
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.ui.home.HomeViewModel
import com.google.gson.Gson

data class Category(val name: String, val imageUrl: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel,
    mainViewModel: MainViewModel
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tìm Kiếm",
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) },
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(it)) {
            val track by viewModel.track.collectAsState()
            val query by viewModel.searchQuery.collectAsState()
            val focusManager = LocalFocusManager.current
            // Thanh tìm kiếm

            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(text = "Tìm kiếm bài hát...", style = TextStyle(color = Color.Gray)) } ,
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search, // Nhấn Enter sẽ thực hiện tìm kiếm
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (query.isNotBlank()) {
                            viewModel.searchTracks(query) // Gọi API tìm kiếm
                            focusManager.clearFocus() // Ẩn bàn phím sau khi tìm kiếm
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(track) { clickedTrack -> // Đổi tên biến để rõ ràng
                    TrackItem(clickedTrack, onTrackClick = {
                        val trackIndex = track.indexOfFirst { it.id == clickedTrack.id }
                        val tracksJson = Uri.encode(Gson().toJson(track)) // Truyền cả danh sách
                        navController.navigate("player/$tracksJson/$trackIndex")
                    })
                }
            }

        }
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



@Composable
fun SearchBar(query: String, onQueryChanged: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("Tìm kiếm bài hát...") },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp)
    )
}







