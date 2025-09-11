package com.example.simplenote.ui.screens.home

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplenote.R

private const val TAG_HOME_EMPTY = "HomeEmptyUI"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeEmptyScreen(
    onCreateNote: () -> Unit,
    onClickHome: () -> Unit = {},
    onClickSettings: () -> Unit = {}
) {
    val purple = Color(0xFF4F46E5)
    val textPrimary = Color(0xFF111827)
    val textSecondary = Color(0xFF6B7280)

    Scaffold(containerColor = Color(0xFFF6F5FB)) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SafeImage(R.drawable.home0, 220.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Start Your Journey",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Every big step start with small step.\nNotes your first idea and start your journey!",
                    fontSize = 14.sp,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(120.dp))
            }

            // Bottom bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                HorizontalDivider(color = Color(0x11000000))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BottomItem(
                        icon = R.drawable.home,
                        label = "Home",
                        active = true,
                        activeColor = purple,
                        inactiveColor = Color(0xFF9CA3AF),
                        onClick = onClickHome
                    )
                    Spacer(Modifier.width(56.dp))
                    BottomItem(
                        icon = R.drawable.cog,
                        label = "Settings",
                        active = false,
                        activeColor = purple,
                        inactiveColor = Color(0xFF9CA3AF),
                        onClick = onClickSettings
                    )
                }
            }

            // FAB وسط (64dp + سایه)
            FloatingActionButton(
                onClick = {
                    Log.d(TAG_HOME_EMPTY, "FAB clicked -> onCreateNote()")
                    onCreateNote()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .size(64.dp),
                containerColor = purple,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp)
            ) {
                SafeImage(R.drawable.plus, 24.dp)
            }
        }
    }
}

/* --- shared bits (از نسخه‌ی قبلی‌ت نگه داشتم) --- */

@Composable
private fun BottomItem(
    @DrawableRes icon: Int,
    label: String,
    active: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    val color = if (active) activeColor else inactiveColor
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 64.dp)) {
        IconButton(onClick = onClick) { SafeImage(icon, 22.dp) }
        Text(label, fontSize = 12.sp, color = color)
    }
}

@Composable
private fun SafeImage(@DrawableRes id: Int, size: Dp) {
    val p = runCatching { painterResource(id) }.getOrNull()
    if (p != null) Image(painter = p, contentDescription = null, modifier = Modifier.size(size))
}
