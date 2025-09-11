package com.example.simplenote.ui.screens.note

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.R

private const val TAG_NOTE_UI = "NoteUI"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Int?,                    // null = create
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    val ctx = LocalContext.current
    val vm: NoteViewModel = viewModel(factory = NoteViewModel.factory(ctx, noteId))
    val state by vm.state.collectAsState()

    val purple = Color(0xFF4F46E5)
    val textPrimary = Color(0xFF111827)
    val textSecondary = Color(0xFF6B7280)
    val divider = Color(0x1A000000)

    if (state.showDeleteSheet) {
        DeleteBottomSheet(
            onDismiss = { vm.toggleDeleteSheet(false) },
            onDelete = {
                vm.toggleDeleteSheet(false)
                vm.delete(onDeleted)
            }
        )
    }

    Scaffold(containerColor = Color.White) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        Log.d(TAG_NOTE_UI, "Back clicked -> save & back")
                        vm.save(onSaved = onSaved)
                        onBack()
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SafeImage(R.drawable.cheveron_left, 18.dp)
                            Spacer(Modifier.width(4.dp))
                            Text("Back", color = purple, fontSize = 16.sp)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    SafeImage(R.drawable.menu, 18.dp)
                }
                Divider(color = divider)

                Column(Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(16.dp))

                    // Title
                    TextField(
                        value = state.title,
                        onValueChange = vm::onTitleChange,
                        textStyle = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary,
                            lineHeight = 36.sp
                        ),
                        placeholder = {
                            Text(
                                "Title",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = purple
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // Content
                    TextField(
                        value = state.content,
                        onValueChange = vm::onContentChange,
                        textStyle = TextStyle(fontSize = 16.sp, color = textSecondary),
                        placeholder = { Text("Feel Free to Write Here...", color = Color(0xFF9CA3AF)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = purple
                        )
                    )

                    Spacer(Modifier.height(24.dp))
                }

                Spacer(Modifier.height(72.dp))
            }

            // Bottom-left
            Text(
                state.lastEditedText,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                color = Color(0xFF111827),
                fontSize = 12.sp
            )

            // Bottom-right delete
            Button(
                onClick = { vm.toggleDeleteSheet(true) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = 64.dp, height = 56.dp),
                shape = RoundedCornerShape(topStart = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = purple, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) { SafeImage(R.drawable.trash, 22.dp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteBottomSheet(
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Want to Delete this Note?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
                IconButton(onClick = onDismiss) { SafeImage(R.drawable.x, 16.dp) }
            }
            Divider(color = Color(0x1A000000))
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SafeImage(R.drawable.trash, 18.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Note", color = Color(0xFFD14343))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SafeImage(@DrawableRes id: Int, size: Dp) {
    runCatching { painterResource(id) }.getOrNull()?.let { p ->
        Image(painter = p, contentDescription = null, modifier = Modifier.size(size))
    }
}
