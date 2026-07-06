package com.example.keepsake.ui.notes_list

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesListViewModel,
    onNavigateToDetail: (Int, Boolean) -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit
) {
    val pinnedNotes by viewModel.pinnedNotes.collectAsState()
    val otherNotes by viewModel.otherNotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsState()

    val isSelectionMode = selectedNoteIds.isNotEmpty()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isDarkTheme = isSystemInDarkTheme()
    val bgColor = if (isDarkTheme) Color(0xFF202124) else Color.White
    val contentColor = if (isDarkTheme) Color.White else Color.Black
    val searchBarColor = if (isDarkTheme) Color(0xFF303134) else Color(0xFFF1F3F4)

    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = bgColor,
                drawerContentColor = contentColor
            ) {
                Text(
                    text = "Keepsake",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(24.dp)
                )
                Divider(color = Color.Gray.copy(alpha = 0.2f))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Lightbulb, contentDescription = "Notes") },
                    label = { Text("Notes") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = contentColor.copy(alpha = 0.1f)
                    )
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Archive, contentDescription = "Archive") },
                    label = { Text("Archive") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToArchive()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = contentColor.copy(alpha = 0.1f)
                    )
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Delete, contentDescription = "Trash") },
                    label = { Text("Trash") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToTrash()
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = contentColor.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = bgColor,
            topBar = {
                if (isSelectionMode) {
                    TopAppBar(
                        title = { Text("${selectedNoteIds.size}", color = contentColor) },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.clearSelection() }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Clear selection",
                                    tint = contentColor
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.archiveSelectedNotes() }) {
                                Icon(
                                    Icons.Outlined.Archive,
                                    contentDescription = "Archive",
                                    tint = contentColor
                                )
                            }
                            IconButton(onClick = { viewModel.deleteSelectedNotes() }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = contentColor
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = searchBarColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = "Menu",
                                    tint = contentColor
                                )
                            }
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = { Text("Search your notes", color = Color.Gray) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = contentColor
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Clear search",
                                        tint = contentColor
                                    )
                                }
                            }
                        }
                    }
                }
            },
            floatingActionButton = {
                if (!isSelectionMode) {
                    FloatingActionButton(
                        onClick = { onNavigateToDetail(-1, false) },
                        containerColor = contentColor,
                        contentColor = bgColor,
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Note",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pinnedNotes.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "PINNED",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                        )
                    }
                    items(pinnedNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            isSelected = selectedNoteIds.contains(note.id),
                            showPinIcon = true,
                            onClick = {
                                if (isSelectionMode) viewModel.toggleSelection(note.id)
                                else onNavigateToDetail(note.id, note.isListMode)
                            },
                            onLongClick = { viewModel.toggleSelection(note.id) },
                            onTogglePin = { viewModel.togglePin(note) }
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            "OTHERS",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp, top = 16.dp)
                        )
                    }
                }

                items(otherNotes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        isSelected = selectedNoteIds.contains(note.id),
                        showPinIcon = true,
                        onClick = {
                            if (isSelectionMode) viewModel.toggleSelection(note.id)
                            else onNavigateToDetail(note.id, note.isListMode)
                        },
                        onLongClick = { viewModel.toggleSelection(note.id) },
                        onTogglePin = { viewModel.togglePin(note) }
                    )
                }
            }
        }
    }
}