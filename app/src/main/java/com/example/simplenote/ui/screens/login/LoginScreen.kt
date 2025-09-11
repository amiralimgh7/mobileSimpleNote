package com.example.simplenote.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val vm: LoginViewModel = viewModel()
    val state = vm.state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(state.success) {
        if (state.success) onLoggedIn()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // فاصله‌ی بالای صفحه مثل فیگما
            Spacer(Modifier.height(28.dp))

            // عنوان
            Text(
                text = "Let’s Login",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827) // تقریباً همون مشکی نرم فیگما
            )
            Spacer(Modifier.height(6.dp))
            // زیرعنوان
            Text(
                text = "And notes your idea",
                fontSize = 14.sp,
                color = Color(0xFF6B7280)
            )

            Spacer(Modifier.height(24.dp))

            // لیبل ایمیل
            Text(
                text = "Email Address",
                fontSize = 14.sp,
                color = Color(0xFF111827),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.identity,
                onValueChange = vm::onIdentityChange,
                placeholder = { Text("Example: johndoe@gmail.com", color = Color(0xFF9CA3AF)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF4F46E5),
                    focusedLabelColor = Color(0xFF111827)
                )
            )

            Spacer(Modifier.height(16.dp))

            // لیبل پسورد
            Text(
                text = "Password",
                fontSize = 14.sp,
                color = Color(0xFF111827),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                placeholder = { Text("********", color = Color(0xFF9CA3AF)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (!state.loading) vm.login() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF4F46E5),
                    focusedLabelColor = Color(0xFF111827)
                )
            )

            Spacer(Modifier.height(24.dp))

            // دکمه‌ی اصلی بنفش کپسولی با فلش راست
            Button(
                onClick = { if (!state.loading) vm.login() },
                enabled = !state.loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF4F46E5).copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        text = if (state.loading) "Please wait…" else "Login",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Image(
                        painter = painterResource(id = R.drawable.arrow_right),
                        contentDescription = null,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.CenterEnd)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Divider با "Or"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(color = Color(0xFFE5E7EB), modifier = Modifier.weight(1f))
                Text("  Or  ", color = Color(0xFF9CA3AF))
                Divider(color = Color(0xFFE5E7EB), modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // لینک ثبت‌نام
            TextButton(onClick = onNavigateToRegister) {
                val s = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF6B7280))) { append("Don’t have any account? ") }
                    withStyle(SpanStyle(color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)) { append("Register here") }
                }
                Text(
                    s,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
