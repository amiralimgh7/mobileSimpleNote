package com.example.simplenote.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val purple = Color(0xFF504EC3)
    val textPrimary = Color(0xFF111827)
    val textSecondary = Color(0xFF6B7280)

    val vm: SettingsViewModel =
        viewModel(factory = SettingsViewModel.factory(LocalContext.current))
    val ui by vm.state.collectAsState()

    var showLogout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = textPrimary) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        SafeImage(R.drawable.arrow_left, 18.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Back", color = purple, fontSize = 14.sp)
                    }
                }
            )
        },
        containerColor = Color.White
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Profile Card (name/email از state)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF6F5FB))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAE9F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        SafeImage(R.drawable.user, 24.dp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = ui.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SafeImage(R.drawable.mail, 14.dp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = ui.email,
                                color = textSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // خطا اگر پر شده
                ui.error?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = it, color = Color(0xFFD14343), fontSize = 12.sp)
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color(0x11000000))
                Spacer(Modifier.height(12.dp))
                Text("APP SETTINGS", color = textSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))

                // Change Password
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onChangePassword() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 16.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Change Password",
                        color = textPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text("›", color = textSecondary, fontSize = 22.sp)
                }

                Spacer(Modifier.height(8.dp))
                // Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showLogout = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SafeImage(R.drawable.logout, 20.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Log Out", color = Color(0xFFD14343), fontSize = 16.sp)
                }

                Spacer(Modifier.weight(1f))
                Text(
                    "Taha Notes v1.1",
                    color = Color(0xFFB8B8C8),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            if (showLogout) {
                AlertDialog(
                    onDismissRequest = { showLogout = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLogout = false
                                vm.logout()
                                onLoggedOut()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = purple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) { Text("Yes") }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showLogout = false },
                            shape = RoundedCornerShape(24.dp)
                        ) { Text("Cancel", color = purple) }
                    },
                    title = {
                        Text("Log Out", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    },
                    text = {
                        Text(
                            "Are you sure you want to log out from the application?",
                            color = Color(0xFF6B7280)
                        )
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SafeImage(id: Int, size: Dp) {
    val p = runCatching { painterResource(id) }.getOrNull()
    if (p != null) Image(painter = p, contentDescription = null, modifier = Modifier.size(size))
}
