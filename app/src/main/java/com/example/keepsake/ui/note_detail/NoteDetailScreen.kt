package com.example.keepsake.ui.note_detail

import android.Manifest
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.keepsake.ui.components.ZoomableImage
import com.example.keepsake.ui.theme.DarkNoteColors
import com.example.keepsake.ui.theme.LightNoteColors
import com.example.keepsake.ui.theme.getThemeNoteColor
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    isListModeInitial: Boolean = false,
    viewModel: NoteDetailViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId, isListModeInitial)
    }

    val availableColors = if (isDarkTheme) DarkNoteColors else LightNoteColors
    val currentBackgroundColor = getThemeNoteColor(viewModel.noteColor, isDarkTheme)
    val contentColor = if (isDarkTheme) Color.White else Color.Black

    BackHandler {
        viewModel.saveNote()
        onBackClick()
    }

    val sheetState = rememberModalBottomSheetState()
    var showColorBottomSheet by rememberSaveable { mutableStateOf(false) }
    var fullScreenImagePath by rememberSaveable { mutableStateOf<String?>(null) }

    // Audio State
    var isRecording by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.release()
            player?.release()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecording = true
            val file = File(context.filesDir, "audio_${System.currentTimeMillis()}.mp3")
            viewModel.updateAudioPath(file.absolutePath)
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = File(context.filesDir, "img_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                viewModel.addImagePath(file.absolutePath)
            }
        }
    )

    if (showColorBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showColorBottomSheet = false },
            sheetState = sheetState,
            containerColor = currentBackgroundColor,
            contentColor = contentColor
        ) {
            Column(modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)) {
                Text(
                    "Color",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    items(availableColors) { colorHex ->
                        val isSelected = viewModel.noteColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) contentColor else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.updateColor(colorHex)
                                }
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = currentBackgroundColor,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveNote()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = contentColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePin() }) {
                        Icon(
                            imageVector = if (viewModel.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = contentColor
                        )
                    }
                    IconButton(onClick = { viewModel.toggleArchive() }) {
                        Icon(
                            imageVector = if (viewModel.isArchived) Icons.Filled.Archive else Icons.Outlined.Archive,
                            contentDescription = "Archive",
                            tint = contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                contentPadding = PaddingValues(horizontal = 4.dp),
                actions = {
                    IconButton(onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(Icons.Outlined.AddBox, contentDescription = null, tint = contentColor)
                    }
                    IconButton(onClick = { showColorBottomSheet = true }) {
                        Icon(Icons.Outlined.Palette, contentDescription = null, tint = contentColor)
                    }
                    IconButton(onClick = {
                        if (isRecording) {
                            recorder?.stop()
                            recorder?.release()
                            recorder = null
                            isRecording = false
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.StopCircle else Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isRecording) Color.Red else contentColor
                        )
                    }
                },
                floatingActionButton = {
                    IconButton(onClick = {
                        viewModel.moveToTrash()
                        onBackClick()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = contentColor)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (viewModel.imagePaths.isNotEmpty() || viewModel.drawingPaths.isNotEmpty() || viewModel.audioPath != null) {
                Column {
                    if (viewModel.imagePaths.isNotEmpty() || viewModel.drawingPaths.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.imagePaths) { path ->
                                Box {
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(150.dp)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { fullScreenImagePath = path },
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeImagePath(path) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            items(viewModel.drawingPaths) { path ->
                                Box {
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(150.dp)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black)
                                            .clickable { fullScreenImagePath = path },
                                        contentScale = ContentScale.Fit
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeDrawingPath(path) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    viewModel.audioPath?.let { path ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = contentColor.copy(alpha = 0.1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                IconButton(onClick = {
                                    if (isPlaying) {
                                        player?.stop()
                                        player?.release()
                                        player = null
                                        isPlaying = false
                                    } else {
                                        isPlaying = true
                                        player = MediaPlayer().apply {
                                            setDataSource(path)
                                            prepare()
                                            start()
                                            setOnCompletionListener { isPlaying = false }
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = contentColor
                                    )
                                }
                                Text(
                                    "Voice note",
                                    modifier = Modifier.weight(1f),
                                    color = contentColor
                                )
                                IconButton(onClick = {
                                    viewModel.updateAudioPath(null)
                                    if (isPlaying) player?.stop()
                                }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = contentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            TextField(
                value = viewModel.noteTitle,
                onValueChange = { viewModel.updateTitle(it) },
                placeholder = { Text("Title", fontSize = 22.sp, color = Color.Gray) },
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = contentColor
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.weight(1f)) {
                if (viewModel.isListMode) {
                    LazyColumn(modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)) {
                        items(viewModel.checklistItems, key = { it.id }) { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.DragIndicator,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = 4.dp)
                                )
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = {
                                        viewModel.updateChecklistItem(
                                            item.id,
                                            item.text,
                                            it
                                        )
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = contentColor,
                                        checkmarkColor = currentBackgroundColor
                                    )
                                )
                                TextField(
                                    value = item.text,
                                    onValueChange = {
                                        viewModel.updateChecklistItem(
                                            item.id,
                                            it,
                                            item.isChecked
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = contentColor
                                    ),
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        color = if (item.isChecked) Color.Gray else contentColor,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.removeChecklistItem(item.id) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                        item {
                            var newItemText by remember { mutableStateOf("") }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                                )
                                TextField(
                                    value = newItemText,
                                    onValueChange = { newItemText = it },
                                    placeholder = { Text("List item", color = Color.Gray) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = contentColor
                                    ),
                                    textStyle = TextStyle(fontSize = 16.sp, color = contentColor),
                                    modifier = Modifier.weight(1f)
                                )
                                if (newItemText.isNotBlank()) {
                                    IconButton(onClick = {
                                        viewModel.addChecklistItem(newItemText); newItemText = ""
                                    }) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = contentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    TextField(
                        value = viewModel.noteContent,
                        onValueChange = { viewModel.updateContent(it) },
                        placeholder = { Text("Note", fontSize = 16.sp, color = Color.Gray) },
                        textStyle = TextStyle(fontSize = 16.sp, color = contentColor),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = contentColor
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (fullScreenImagePath != null) {
        Dialog(
            onDismissRequest = { fullScreenImagePath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)) {
                ZoomableImage(
                    imageFile = File(fullScreenImagePath!!),
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { fullScreenImagePath = null },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}