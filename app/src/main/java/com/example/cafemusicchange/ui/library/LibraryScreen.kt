package com.example.cafemusicchange.ui.playlist
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cafemusicchange.R
import com.example.cafemusicchange.local.Playlist
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


const val DEFAULT_PLAYLIST_IMAGE = "https://source.unsplash.com/random/300x300"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    navController: NavHostController,
    viewModel: PlaylistViewModel = hiltViewModel(),
    userId: Long
) {
    val playlists by viewModel.userPlaylists.collectAsState()

    // State để mở/đóng Bottom Sheet
    var showBottomSheet by remember { mutableStateOf(false) }

    // Khi mở màn hình, tự động load danh sách playlist
    LaunchedEffect(userId) {
        viewModel.loadUserPlaylists(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thư viện", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) },
//                actions = {
//                    IconButton(onClick = { /* Mở tìm kiếm */ }) {
//                        Icon(imageVector = Icons.Default.Search, contentDescription = "Tìm kiếm Playlist")
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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LibraryCategoryCard(
                    modifier = Modifier.size(110.dp),
                    iconRes = R.drawable.like,
                    label   = "Yêu thích",
                    onClick = {navController.navigate("favorite/$userId") }
                )
                LibraryCategoryCard(
                    modifier = Modifier.size(110.dp),
                    iconRes = R.drawable.downloading,
                    label   = "Tải về",
                    onClick = { /*…*/ }
                )
                LibraryCategoryCard(
                    modifier = Modifier.size(110.dp),
                    iconRes = R.drawable.historical,
                    label   = "Gần đây",
                    onClick = { /*…*/ }
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            Text("Danh sách phát", fontSize = 20.sp, fontWeight = FontWeight.Medium)

            LazyColumn {
                items(playlists) { playlist ->
                    PlaylistItem(
                        playlist = playlist,
                        onClick = {
                            navController.navigate("playlist_detail/${playlist.playlistId}/$userId")
                        },
                        onDelete = { playlistId ->
                            viewModel.deletePlaylist(playlistId,userId) // Xoá playlist từ ViewModel
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBottomSheet = true } // Khi nhấn, mở Bottom Sheet
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm Playlist", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm playlist", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    // Hiển thị Bottom Sheet khi `showBottomSheet = true`


    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddPlaylistBottomSheet(
                onDismiss = { showBottomSheet = false },
                onAddPlaylist = { name, imageUri ->
                    viewModel.addPlaylist(name, userId, imageUri)
                }
            )
        }
    }

}





@Composable
fun AddPlaylistBottomSheet(
    onDismiss: () -> Unit,
    onAddPlaylist: (String, String?) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {

        Text("Tạo Playlist Mới", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            label = { Text("Tên Playlist") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .clickable { imagePickerLauncher.launch("image/*") }
        ) {
            if (selectedImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImageUri),
                    contentDescription = "Ảnh Playlist",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    "Chọn ảnh cho Playlist",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val imagePath = selectedImageUri?.let { saveImageToInternalStorage(context, it) }
                onAddPlaylist(playlistName, imagePath) // ✅ Không truyền Context, chỉ truyền đường dẫn ảnh
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Thêm Playlist")
        }
    }
}



@Composable
fun PlaylistItem(playlist: Playlist, onClick: () -> Unit, onDelete: (Long) -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val painter = rememberAsyncImagePainter(
            if (playlist.albumImage != null) File(playlist.albumImage) else DEFAULT_PLAYLIST_IMAGE
        )

        Image(
            painter = painter,
            contentDescription = "Playlist Image",
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = playlist.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }

        // Icon ba chấm tuỳ chọn
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Tùy chọn")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Xóa Playlist") },
                    onClick = {
                        showMenu = false
                        onDelete(playlist.playlistId) // Xóa playlist theo ID
                    }
                )
            }
        }
    }
}

fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
    val fileName = "playlist_${System.currentTimeMillis()}.jpg"
    val imageDir = File(context.filesDir, "playlist_images").apply { mkdirs() }
    val imageFile = File(imageDir, fileName)

    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        val outputStream = FileOutputStream(imageFile)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        imageFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun LibraryCategoryCard(
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    // Column để chứa Card vuông và label bên dưới
    Column(
        modifier = modifier.width(50.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Card vuông: rộng bằng hết width của Column, cao = width nhờ aspectRatio
        Card(
            modifier = Modifier
                .size(80.dp),
               // .aspectRatio(1f)
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Label hiển thị bên dưới Card, căn giữa
        Text(
            text = label,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun HeartCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (checked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (checked) Color.Red else Color.Gray
        )
    }
}


