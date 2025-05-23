

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.cafemusicchange.MainViewModel
import com.example.cafemusicchange.api.JamendoTrack
import com.example.cafemusicchange.ui.playback.MusicPlayer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun PlayScreen(
    tracksJson: String,
    startIndex: Int,
    viewModel: MusicPlayer = hiltViewModel(),
    navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    val userId = mainViewModel.uiState.collectAsState().value.userId
    LaunchedEffect(tracksJson, startIndex) {
        if (userId != null) {
            viewModel.setQueue(tracksJson, startIndex,userId)
        }
    }

    CustomExoPlayerScreen(viewModel = viewModel, navHostController,mainViewModel)
}



