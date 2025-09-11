package com.example.simplenote.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    val vm: RegisterViewModel = viewModel()
    val state = vm.state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) { state.error?.let { snackbarHostState.showSnackbar(it) } }
    LaunchedEffect(state.success) { if (state.success) onRegistered() }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(enabled = !state.loading) { onBack() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp).padding(end = 6.dp)
                )
                Text("Back to Login", color = Color(0xFF4F46E5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))

            Text("Register", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
            Spacer(Modifier.height(6.dp))
            Text("And start taking notes", fontSize = 14.sp, color = Color(0xFF6B7280))

            Spacer(Modifier.height(24.dp))

            Text("First Name", fontSize = 14.sp, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.firstName,
                onValueChange = vm::onFirstNameChange,
                placeholder = { Text("Example: Taha", color = Color(0xFF9CA3AF)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF4F46E5)
                )
            )

            Spacer(Modifier.height(16.dp))

            Text("Last Name", fontSize = 14.sp, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.lastName,
                onValueChange = vm::onLastNameChange,
                placeholder = { Text("Example: Hamifar", color = Color(0xFF9CA3AF)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF4F46E5)
                )
            )

            Spacer(Modifier.height(16.dp))

            Text("Username", fontSize = 14.sp, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.username,
                onValueChange = vm::onUsernameChange,
                placeholder = { Text("Example: @HamifarTaha", color = Color(0xFF9CA3AF)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF4F46E5)
                )
            )

            Spacer(Modifier.height(16.dp))

            Text("Email Address", fontSize = 14.sp, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                placeholder = { Text("Example: hamifar.taha@gmail.com", color = Color(0xFF9CA3AF)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF4F46E5)
                )
            )

            Spacer(Modifier.height(16.dp))

            Text("Password", fontSize = 14.sp, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                placeholder = { Text("********", color = Color(0xFF9CA3AF)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF4F46E5)
                )
            )

            Spacer(Modifier.height(16.dp))

            Text("Retype Password", fontSize = 14.sp, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.retype,
                onValueChange = vm::onRetypeChange,
                placeholder = { Text("********", color = Color(0xFF9CA3AF)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (!state.loading) vm.submit() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFE5E7EB),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF4F46E5)
                )
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { if (!state.loading) vm.submit() },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
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
                        text = if (state.loading) "Please wait…" else "Register",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Image(
                        painter = painterResource(id = R.drawable.arrow_right),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp).align(Alignment.CenterEnd)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            TextButton(onClick = { if (!state.loading) onBack() }) {
                val s = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFF6B7280))) { append("Already have an account? ") }
                    withStyle(SpanStyle(color = Color(0xFF4F46E5), fontWeight = FontWeight.SemiBold)) { append("Login here") }
                }
                Text(s, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontSize = 14.sp)
            }

            Spacer(Modifier.height(28.dp))
        }
    }
}
