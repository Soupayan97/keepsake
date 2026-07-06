package com.example.keepsake.ui.notes_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: NotesListViewModel,
    onNavigateBack: () -> Unit
) {
    val trashedNotes by viewModel.trashedNotes.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()
    val bgColor = if (isDarkTheme) Color(0xFF202124) else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    val popupBgColor = if (isDarkTheme) Color(0xFF2D2E33) else Color.White
    val buttonTextColor = if (isDarkTheme) Color(0xFFAECBFA) else Color(0xFF1A73E8)

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var selectedNoteId by rememberSaveable { mutableStateOf<Int?>(null) }

    val selectedNote = remember(selectedNoteId, trashedNotes) {
        trashedNotes.find { it.id == selectedNoteId }
    }

    if (showDialog && selectedNote != null) {
        Dialog(onDismissRequest = { showDialog = false; selectedNoteId = null }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = popupBgColor,
                tonalElevation = 4.dp,
                border = if (!isDarkTheme) BorderStroke(
                    1.dp,
                    Color.LightGray.copy(alpha = 0.5f)
                ) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Delete note forever?",
                        fontSize = 18.sp,
                        color = contentColor,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false; selectedNoteId = null }) {
                            Text("Cancel", color = buttonTextColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            selectedNote.let { viewModel.deleteNotePermanently(it) }
                            showDialog = false
                            selectedNoteId = null
                        }) {
                            Text("Delete", color = buttonTextColor)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            selectedNote.let { viewModel.restoreFromTrash(it) }
                            showDialog = false
                            selectedNoteId = null
                        }) {
                            Text("Restore", color = buttonTextColor)
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("Trash", color = contentColor) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "", tint = contentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if (trashedNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notes in Trash", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(trashedNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = {
                                selectedNoteId = note.id
                                showDialog = true
                            },
                            onLongClick = {},
                            showPinIcon = false,
                            onTogglePin = { viewModel.togglePin(note) }
                        )
                    }
                }
            }
        }
    }
}