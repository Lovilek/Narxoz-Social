package com.narxoz.social.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narxoz.social.R
import android.util.Log
import androidx.navigation.NavController

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val staySignedIn by viewModel.staySignedIn.collectAsState()
    val loginResult by viewModel.loginResult.observeAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Наблюдаем за сообщением об ошибке
    val loginErrorState by viewModel.loginError.observeAsState()

    val errorMessage = loginErrorState

    LaunchedEffect(loginResult) {
        loginResult?.let { role ->
            when (role) {
                "student" -> navController.navigate("student")
                "teacher" -> navController.navigate("teacher")
                "admin" -> navController.navigate("mainFeed")
                else -> { /* Обработка ошибок/неизвестных ролей */ }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.narxoz_logo),
                contentDescription = "Narxoz Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Делаем текст контрастным
            Text(
                text = "SIGN IN",
                fontWeight = FontWeight.Bold,
                color = Color.Black, // контрастный цвет
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = viewModel::updateEmail,
                label = { Text("S/F - Login", color = Color.Gray) },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Password", color = Color.Gray) },
                textStyle = TextStyle(color = Color.Black),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = staySignedIn,
                    onCheckedChange = viewModel::updateStaySignedIn,
                    // сделаем галочку более видимой
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color.Red,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                Text("Stay signed in", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.login(email, password, staySignedIn) },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SIGN IN")
                }
            }

            // Если есть сообщение об ошибке, показываем его под кнопкой
            if (!errorMessage.isNullOrEmpty()) {
                Text(text = errorMessage, color = Color.Red)
            }

            Spacer(modifier = Modifier.height(8.dp))

            ClickableText(
                text = AnnotatedString("Forgot password?"),
                style = TextStyle(color = Color.Black),
                onClick = { /* TODO: переход на восстановление пароля */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            loginResult?.let {
                if (it.isEmpty()) {
                    Text("Ошибка входа", color = Color.Red)
                }
            }
        }
    }
}