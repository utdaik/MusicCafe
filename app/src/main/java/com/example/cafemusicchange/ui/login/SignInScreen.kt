package com.example.cafemusicchange.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cafemusicchange.MainViewModel
import com.example.cafemusicchange.R
import com.example.cafemusicchange.common.enum.LoadStatus
import com.example.cafemusicchange.ui.theme.AlegreyaFontFamily

@Composable
fun SignInScreen(
    navController: NavHostController,
    hiltViewModel: LoginViewModel,
    mainViewModel: MainViewModel
) {
    val uiState by hiltViewModel.uiState.collectAsState()
    var passwordHiddenState by remember { mutableStateOf(true) }

    Surface(
        color = Color(0xFF999933),
        modifier = Modifier.fillMaxSize()
    ) {

        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.bg1),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .align(Alignment.BottomStart)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp)
            ) {

                if(uiState.status is LoadStatus.Loading){
                    CircularProgressIndicator()
                } else if(uiState.status is LoadStatus.Success) {
                    LaunchedEffect(Unit) {
                        uiState.useId.let {
                            if (it != null) {
                                mainViewModel.setUserId(it)
                            }
                        }
                        navController.navigate("home")
                    }
                } else {
                    if (uiState.status is LoadStatus.Error) {
                        mainViewModel.setError(uiState.status.description)
                        hiltViewModel.reset()
                    }

                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 70.dp)
                            .height(100.dp)
                            .align(Alignment.Start)
                    )
                    Text(
                        "Sign In",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight(700)
                        ),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 20.dp)
                    )
                    Text(
                        "Sign in now to access your exercises and save music", // Sửa lỗi chính tả "excercices" thành "exercises"
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight(400)
                        ),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    TextField(
                        value = uiState.email,
                        onValueChange = { hiltViewModel.updateEmail(it) },
                        placeholder = {
                            Text(
                                "Email",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 20.sp,
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.White,
                        )
                    )

                    TextField(
                        value = uiState.password,
                        onValueChange = { hiltViewModel.updatePassword(it) },
                        placeholder = {
                            Text(
                                "Password",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 20.sp,
                                )
                            )
                        },
                        visualTransformation = if (passwordHiddenState) PasswordVisualTransformation() else VisualTransformation.None, // Đổi tên biến để có quy ước đặt tên nhất quán
                        trailingIcon = {
                            val image =
                                if (passwordHiddenState) Icons.Filled.Check else Icons.Filled.Close // Đổi tên biến để có quy ước đặt tên nhất quán

                            IconButton(onClick = {
                                passwordHiddenState = !passwordHiddenState
                            }) { // Đổi tên biến để có quy ước đặt tên nhất quán
                                Icon(
                                    imageVector = image,
                                    contentDescription = if (passwordHiddenState) "Hiện mật khẩu" else "Ẩn mật khẩu" // Đổi tên biến để có quy ước đặt tên nhất quán
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        maxLines = 1,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.White,
                        )
                    )

                    Button(
                        onClick = {
                            hiltViewModel.login(uiState.email, uiState.password)
                        },
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                    ) {
                        Text(
                            "Sign in",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight(700),
                                fontFamily = AlegreyaFontFamily,
                                shadow = Shadow(
                                    color = Color.Black,
                                )
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 12.dp, bottom = 52.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Don't have an account?",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 20.sp,
                            )
                        )
                        TextButton(
                            onClick = { navController.navigate("signup") },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "Sign Up",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight(800),
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// Đã thêm @Preview để hiển thị bản xem trước của composable
@Preview(apiLevel = 34)
@Composable
fun SignInScreenPreview() {
    // Cung cấp các triển khai giả cho preview
    SignInScreen(
        navController = rememberNavController(),
        hiltViewModel = LoginViewModel(null), // Đảm bảo LoginViewModel có constructor không tham số hoặc giá trị mặc định
        mainViewModel = MainViewModel() // Đảm bảo MainViewModel có constructor không tham số hoặc giá trị mặc định
    )
}