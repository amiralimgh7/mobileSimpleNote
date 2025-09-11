package com.example.simplenote.ui.screens.home

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenote.R

private const val TAG_HOME_NOTES = "HomeNotesUI"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNotesScreen(
    onCreateNote: () -> Unit,
    onOpenNote: (Int) -> Unit,
    onClickSettings: () -> Unit
) {
    val vm: HomeViewModel =
        viewModel(factory = HomeViewModel.factory(LocalContext.current))
    val state by vm.state.collectAsState()

    val purple = Color(0xFF4F46E5)
    val textPrimary = Color(0xFF111827)
    val textSecondary = Color(0xFF6B7280)
    val surface = Color(0xFFF6F5FB)

    Scaffold(
        containerColor = surface,
        topBar = {
            // Search bar: هم‌تراز با گرید، 48dp ارتفاع، گوشه 12، سفید
            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 6.dp)
            ) {
                TextField(
                    value = state.query,
                    onValueChange = {
                        Log.d(TAG_HOME_NOTES, "Search query: $it")
                        vm.onQueryChange(it)
                    },
                    placeholder = { Text("Search…", color = Color(0xFF9CA3AF)) },
                    leadingIcon = { SafeImage(R.drawable.search, 18.dp) },
                    trailingIcon = {
                        if (state.query.isNotBlank()) {
                            IconButton(onClick = { vm.onQueryChange("") }) {
                                // اگر آیکن x موجود نبود، هیچ اتفاقی نمی‌افته
                                SafeImage(R.drawable.x, 16.dp)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = purple
                    )
                    )

            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // حالت لودینگ اولیه (وقتی هنوز چیزی نداریم)
            if (state.isLoading && state.notes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Notes",
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            val info = if (state.totalPages > 1)
                                "Page ${state.page} of ${state.totalPages} • ${state.count} items"
                            else
                                "${state.count} items"
                            Text(info, color = textSecondary, fontSize = 12.sp)
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(4.dp)) }

                    // خطای سراسری
                    if (state.error != null && state.notes.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.error ?: "",
                                    color = Color(0xFFD14343),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(6.dp))
                                TextButton(onClick = { vm.refresh() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    // «نتیجهٔ جست‌وجو خالی است» (بدون رفتن به EmptyScreen)
                    if (state.error == null && state.notes.isEmpty() && state.query.isNotBlank()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "No results for “${state.query}”",
                                    color = textPrimary,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Try different keywords or clear the search.",
                                    color = textSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(8.dp))
                                TextButton(
                                    onClick = { vm.onQueryChange("") },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Clear search", color = purple)
                                }
                            }
                        }
                    }

                    // کارت‌های نوت
                    items(state.notes, key = { it.id }) { note ->
                        val bg = if (note.id % 2 == 0) Color(0xFFFFF4CC) else Color(0xFFFFF0B3)
                        NoteCard(
                            bg = bg,
                            title = note.title,
                            desc = note.description,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = { onOpenNote(note.id) }
                        )
                    }

                    // کنترل‌های صفحه‌بندی (ثابت، اما دکمه‌ها هوشمندانه)
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier.height(8.dp))
                        PaginationBar(
                            isLoading = state.isLoading,
                            page = state.page,
                            totalPages = state.totalPages,
                            hasPrev = state.hasPrev,
                            hasNext = state.hasNext,
                            onPrev = { vm.prevPage() },
                            onNext = { vm.nextPage() }
                        )
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }

            // Bottom bar
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                BottomBar(
                    activeColor = purple,
                    onClickHome = { /* همین صفحه */ },
                    onClickSettings = onClickSettings
                )
            }

            // FAB
            FloatingActionButton(
                onClick = onCreateNote,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .size(64.dp),
                shape = CircleShape,
                containerColor = purple,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp)
            ) {
                SafeImage(R.drawable.plus, 24.dp)
            }
        }
    }
}

/* ----------------- Pagination Bar ----------------- */
@Composable
private fun PaginationBar(
    isLoading: Boolean,
    page: Int,
    totalPages: Int,
    hasPrev: Boolean,
    hasNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val textSecondary = Color(0xFF6B7280)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(
            onClick = onPrev,
            enabled = hasPrev && !isLoading,
            shape = RoundedCornerShape(10.dp)
        ) { Text("Previous") }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text("Page $page of $totalPages", color = textSecondary, fontSize = 12.sp)
        }

        TextButton(
            onClick = onNext,
            enabled = hasNext && !isLoading,
            shape = RoundedCornerShape(10.dp)
        ) { Text("Next") }
    }
}

/* ----------------- UI Helpers ----------------- */

@Composable
private fun NoteCard(
    bg: Color,
    title: String?,
    desc: String?,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SafeImage(R.drawable.light_bulb, 16.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = title.orEmpty(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = textPrimary,
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = desc.orEmpty(),
            fontSize = 12.sp,
            color = textSecondary,
            lineHeight = 16.sp,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BottomBar(
    activeColor: Color,
    onClickHome: () -> Unit,
    onClickSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                activeColor = activeColor,
                inactiveColor = Color(0xFF9CA3AF),
                onClick = onClickHome
            )
            Spacer(Modifier.width(56.dp))
            BottomItem(
                icon = R.drawable.cog,
                label = "Settings",
                active = false,
                activeColor = activeColor,
                inactiveColor = Color(0xFF9CA3AF),
                onClick = onClickSettings
            )
        }
    }
}

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
