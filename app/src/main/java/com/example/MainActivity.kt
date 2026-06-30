package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.repository.SessionRepository
import com.example.ui.screens.CoachGuideScreen
import com.example.ui.screens.FastSimulationScreen
import com.example.ui.screens.SlotPlayScreen
import com.example.ui.theme.CasinoDark
import com.example.ui.theme.CasinoNeonGold
import com.example.ui.theme.CasinoNeonPink
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SlotViewModel
import com.example.ui.viewmodel.SlotViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize database & repository inside Compose safely using local context
                val context = LocalContext.current.applicationContext
                val database = AppDatabase.getDatabase(context)
                val repository = SessionRepository(database.sessionLogDao())

                // Instantiate our custom ViewModel
                val slotViewModel: SlotViewModel = viewModel(
                    factory = SlotViewModelFactory(context as Application, repository)
                )

                // Navigation State
                var selectedTab by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF13151F),
                            contentColor = Color.White
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                label = { Text("ตู้ฝึกซ้อม", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Slot Play"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CasinoNeonGold,
                                    indicatorColor = CasinoNeonGold,
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = { Text("คำนวณสูตร", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Risk Backtester"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = CasinoNeonPink,
                                    indicatorColor = CasinoNeonPink,
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )

                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                label = { Text("คู่มือ & บันทึก", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Guide & Logs"
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CasinoNeonGold,
                                    indicatorColor = CasinoNeonGold,
                                    unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                    unselectedTextColor = Color.White.copy(alpha = 0.4f)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CasinoDark)
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> SlotPlayScreen(viewModel = slotViewModel)
                            1 -> FastSimulationScreen(viewModel = slotViewModel)
                            2 -> CoachGuideScreen(viewModel = slotViewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
