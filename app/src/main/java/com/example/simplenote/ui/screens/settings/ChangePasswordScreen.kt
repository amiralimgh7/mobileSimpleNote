package com.example.simplenote.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit
) {
    val vm: ChangePasswordViewModel =
        viewModel(factory = ChangePasswordViewModel.factory(LocalContext.current))
    val ui by vm.state.collectAsState()

    val purple = Color(0xFF504EC3)
    val textPrimary = Color(0xFF111827)
    val textSecondary = Color(0xFF6B7280)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password", color = textPrimary) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("< Back", color = purple, fontSize = 14.sp) }
                }
            )
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text("Please input your current password first", color = purple, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ui.current,
                    onValueChange = vm::onCurrentChange,
                    label = { Text("Current Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))
                Divider(color = Color(0x11000000))
                Spacer(Modifier.height(8.dp))

                Text("Now, create your new password", color = purple, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ui.newPass,
                    onValueChange = vm::onNewChange,
                    label = { Text("New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text("Password should contain a-z, A-Z, 0-9", color = textSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = ui.retype,
                    onValueChange = vm::onRetypeChange,
                    label = { Text("Retype New Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = { vm.submit() },
                    enabled = !ui.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = purple,
                        contentColor = Color.White
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                ) {
                    if (ui.isSubmitting) {
                        CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Submit New Password")
                            Spacer(Modifier.width(8.dp))
                            Text("→", fontSize = 18.sp)
                        }
                    }
                }
            }

            ui.message?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    action = {
                        TextButton(onClick = { vm.clearMessage() }) { Text("OK", color = Color.White) }
                    }
                ) { Text(msg) }
            }
        }
    }
}
