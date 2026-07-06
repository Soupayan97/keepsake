package com.example.keepsake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.keepsake.data.local.NoteDatabase
import com.example.keepsake.data.repository.NoteRepository
import com.example.keepsake.ui.note_detail.NoteDetailScreen
import com.example.keepsake.ui.note_detail.NoteDetailViewModel
import com.example.keepsake.ui.notes_list.ArchiveScreen
import com.example.keepsake.ui.notes_list.NotesListScreen
import com.example.keepsake.ui.notes_list.NotesListViewModel
import com.example.keepsake.ui.notes_list.TrashScreen
import com.example.keepsake.ui.theme.KeepsakeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSplashScreen = true
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            delay(1500)
            keepSplashScreen = false
        }
        val database = NoteDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(database.noteDao)
        setContent {
            KeepsakeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val listViewModel: NotesListViewModel = viewModel(
                        factory = NotesListViewModel.Factory(repository)
                    )

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            NotesListScreen(
                                viewModel = listViewModel,
                                onNavigateToDetail = { noteId, isListMode ->
                                    navController.navigate("detail/$noteId?isListMode=$isListMode")
                                },
                                onNavigateToArchive = { navController.navigate("archive") },
                                onNavigateToTrash = { navController.navigate("trash") }
                            )
                        }

                        composable(
                            route = "detail/{noteId}?isListMode={isListMode}",
                            arguments = listOf(
                                navArgument("noteId") { type = NavType.IntType },
                                navArgument("isListMode") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
                            val isListMode =
                                backStackEntry.arguments?.getBoolean("isListMode") ?: false
                            val detailViewModel: NoteDetailViewModel = viewModel(
                                factory = NoteDetailViewModel.Factory(repository)
                            )
                            NoteDetailScreen(
                                noteId = noteId,
                                isListModeInitial = isListMode,
                                viewModel = detailViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable("archive") {
                            ArchiveScreen(
                                viewModel = listViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToDetail = { noteId ->
                                    navController.navigate("detail/$noteId?isListMode=false")
                                }
                            )
                        }

                        composable("trash") {
                            TrashScreen(
                                viewModel = listViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}