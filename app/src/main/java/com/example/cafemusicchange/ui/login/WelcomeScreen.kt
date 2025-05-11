package com.example.cafemusicchange.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cafemusicchange.MainViewModel
import com.example.cafemusicchange.R
import com.example.cafemusicchange.ui.theme.AlegreyaFontFamily


@Composable
fun WelcomeScreen(navController: NavHostController,
) {

            Box(Modifier.fillMaxSize()){
                Image(painter = painterResource(id= R.drawable.background),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    modifier = Modifier.fillMaxSize()
                    )
                Column(modifier = Modifier
                    .fillMaxSize().padding(horizontal = 20.dp)
                    ,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 40.dp)
                            .width(230.dp)
                            .height(150.dp)
                        ,
                        contentScale = ContentScale.Fit
                    )
                    Text(text = "Welcome to Cafe Music",
                        fontFamily = AlegreyaFontFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight(700),
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                blurRadius = 1f
                            )
                        ),
                        modifier = Modifier.padding(16.dp)
                        )
                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {navController.navigate("signin")},
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                    ) {
                        Text("Sign In With Email",
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
                    Row(modifier = Modifier.padding(top = 12.dp, bottom = 52.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Don't have an account?",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 20.sp,
                                fontFamily = AlegreyaFontFamily,
                            )
                        )
                        TextButton(
                            onClick = {navController.navigate("signup")},
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Sign Up",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight(800),
                                    fontFamily = AlegreyaFontFamily,
                                )
                                )
                        }
                    }


                }
            }


}


@Preview
@Composable
fun WelcomPreview(){
    WelcomeScreen(
        navController =  NavHostController(LocalContext.current),
    )
}