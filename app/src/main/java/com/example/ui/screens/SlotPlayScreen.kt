package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BettingStrategy
import com.example.model.SlotSymbol
import com.example.ui.theme.*
import com.example.ui.viewmodel.SlotViewModel
import kotlin.math.roundToInt

// PG Soft Inspired Color Theme
val AztecDark = Color(0xFF140D07)      // Deep earthy dark background
val AztecStone = Color(0xFF23160C)     // Stone grey-brown container
val AztecGold = Color(0xFFFFC400)      // Shimmering Aztec Gold
val AztecJade = Color(0xFF00E676)      // Emerald Jade
val AztecOrange = Color(0xFFFF6D00)    // Fiery Amber
val AztecPurple = Color(0xFFD500F9)    // Neon Aztec Magenta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotPlayScreen(
    viewModel: SlotViewModel,
    modifier: Modifier = Modifier
) {
    val bankroll = viewModel.bankroll
    val currentBet = viewModel.currentBet
    val baseBet = viewModel.baseBet
    val lastWin = viewModel.lastWin
    val isSpinning = viewModel.isSpinning
    val reels = viewModel.reels
    val winningPaylines = viewModel.winningPaylines
    val coachMessage = viewModel.coachMessage
    val coachAdviceType = viewModel.coachAdviceType
    val selectedStrategy = viewModel.selectedStrategy
    
    // PG Soft cascading states
    val currentMultiplier = viewModel.currentMultiplier
    val isCascading = viewModel.isCascading
    val winningPositions = viewModel.winningPositions

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AztecDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. Beautiful Aztec Banner ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(1.5.dp, AztecGold), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.aztec_banner),
                contentDescription = "Treasures of Aztec PG Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Soft Dark Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            // Title Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "TREASURES OF AZTEC",
                    color = AztecGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black, blurRadius = 6f)
                    )
                )
                Text(
                    text = "ตู้จำลองเดินเงินสูตรสล็อต PG Soft",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black, blurRadius = 4f)
                    )
                )
            }
        }

        // --- 2. Aztec HUD Header ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AztecStone),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AztecGold.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bankroll (ทุนคงเหลือ)
                Column {
                    Text(
                        text = "ทุนปัจจุบัน (Bankroll)",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🪙",
                            fontSize = 16.sp
                        )
                        Text(
                            text = String.format("%,.1f", bankroll),
                            color = AztecGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("bankroll_text")
                        )
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )

                // Last Win (ชนะล่าสุด)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "ชนะล่าสุด (Last Win)",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (lastWin > 0) {
                            Text(
                                text = "🎉",
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = if (lastWin > 0) String.format("+%,.1f", lastWin) else "0.0",
                            color = if (lastWin > 0) AztecJade else Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("last_win_text")
                        )
                    }
                }
            }
        }

        // --- 3. PG Multiplier Bar ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AztecStone),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (currentMultiplier > 1) AztecGold else Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "ตัวคูณตัวเกมปัจจุบัน:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isCascading) {
                        Text(
                            text = "ร่วงหล่น (CASCADE)",
                            color = AztecJade,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.scale(1.1f)
                        )
                    }
                    Text(
                        text = "x$currentMultiplier",
                        color = if (currentMultiplier > 1) AztecGold else Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = TextStyle(
                            shadow = if (currentMultiplier > 1) Shadow(color = AztecGold, blurRadius = 8f) else null
                        )
                    )
                }
            }
        }

        // --- 4. Interactive 3x3 Aztec Reels Frame ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2B1C10), Color(0xFF140D07))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    BorderStroke(2.dp, Brush.linearGradient(listOf(AztecGold, AztecJade))),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Gold Temple Pillar Row Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text("🗿", color = AztecGold, fontSize = 12.sp)
                    Text(
                        text = "PG-AZTEC TRAINER ENGINE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    )
                    Text("🗿", color = AztecGold, fontSize = 12.sp)
                }

                // The 3x3 Grid
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (col in 0..2) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (row in 0..2) {
                                val symbol = reels[col][row]
                                // Check if cell is cascading or part of winning line
                                val isWinning = winningPositions.contains(Pair(col, row)) || isCellWinning(col, row, winningPaylines)

                                // Pulsing animation on win
                                val infiniteTransition = rememberInfiniteTransition()
                                val scale by infiniteTransition.animateFloat(
                                    initialValue = 1.0f,
                                    targetValue = if (isWinning) 1.05f else 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(350, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "scale"
                                )

                                val cellBg by animateColorAsState(
                                    targetValue = if (isWinning) AztecOrange.copy(alpha = 0.25f) else AztecStone.copy(alpha = 0.8f),
                                    animationSpec = tween(250),
                                    label = "cellBg"
                                )

                                val borderBrush = if (isWinning) {
                                    Brush.linearGradient(listOf(AztecGold, AztecOrange))
                                } else {
                                    Brush.linearGradient(listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.02f)))
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .scale(scale)
                                        .background(cellBg, RoundedCornerShape(12.dp))
                                        .border(
                                            BorderStroke(if (isWinning) 1.8.dp else 1.dp, borderBrush),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        // Emoji Icon (Aztec mask / symbols)
                                        Text(
                                            text = symbol.icon,
                                            fontSize = 30.sp,
                                            modifier = Modifier.padding(bottom = 1.dp)
                                        )
                                        // Label
                                        Text(
                                            text = symbol.label,
                                            color = if (isWinning) AztecGold else symbol.color,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.4.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Active payline details
                if (winningPaylines.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Win",
                            tint = AztecJade,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "ชนะรวมแนวทางเพย์ไลน์!",
                            color = AztecJade,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 5. AI Coach Strategy Advisor Panel (M3 Container) ---
        val advisorBgColor = when (coachAdviceType) {
            "success" -> AztecJade.copy(alpha = 0.12f)
            "warning" -> AztecOrange.copy(alpha = 0.12f)
            "alert" -> AztecPurple.copy(alpha = 0.12f)
            else -> AztecGold.copy(alpha = 0.12f)
        }

        val advisorBorderColor = when (coachAdviceType) {
            "success" -> AztecJade
            "warning" -> AztecOrange
            "alert" -> AztecPurple
            else -> AztecGold
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = advisorBgColor),
            border = BorderStroke(1.dp, advisorBorderColor.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Floating Robot/Coach icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(advisorBorderColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, advisorBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (coachAdviceType) {
                            "success" -> "👸"
                            "warning" -> "⚠️"
                            "alert" -> "🚨"
                            else -> "🗿"
                        },
                        fontSize = 16.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "คำแนะนำโค้ชสล็อตสาวถ้ำ (Aztec Advice)",
                        color = advisorBorderColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = coachMessage,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // --- 6. Betting controls and Strategy Chips Row ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "เลือกสูตรการเดินเงิน (Betting Strategy PG style):",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 2.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BettingStrategy.values()) { strategy ->
                    val isSelected = selectedStrategy == strategy
                    val chipBgColor = if (isSelected) AztecGold else AztecStone
                    val chipContentColor = if (isSelected) Color.Black else Color.White
                    val borderStrokeColor = if (isSelected) AztecGold else Color.White.copy(alpha = 0.1f)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(chipBgColor)
                            .border(BorderStroke(1.dp, borderStrokeColor), RoundedCornerShape(20.dp))
                            .clickable(enabled = !isSpinning) {
                                viewModel.updateStrategy(strategy)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strategy.nameTh,
                            color = chipContentColor,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Strategy details small description
            Text(
                text = selectedStrategy.description,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)
            )

            // Adjust base bet size & Spin Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Adjust Base Bet (เบทเริ่มต้น)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = AztecStone),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "เบทเริ่มต้น (Base Bet)",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Decrease Bet
                            IconButton(
                                onClick = {
                                    if (baseBet > 5.0) {
                                        viewModel.updateBaseBet(baseBet - 5.0)
                                    }
                                },
                                enabled = !isSpinning && baseBet > 5.0,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Text("-", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(
                                text = baseBet.roundToInt().toString(),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Increase Bet
                            IconButton(
                                onClick = {
                                    viewModel.updateBaseBet(baseBet + 5.0)
                                },
                                enabled = !isSpinning,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // The Big PG Spin Button
                val isAffordable = bankroll >= currentBet && currentBet > 0.0
                val spinBtnColor = if (isSpinning) {
                    AztecStone
                } else if (!isAffordable) {
                    Color.Gray.copy(alpha = 0.2f)
                } else {
                    AztecGold
                }

                Button(
                    onClick = { viewModel.spin() },
                    enabled = !isSpinning && isAffordable,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = spinBtnColor,
                        contentColor = Color.Black,
                        disabledContainerColor = AztecStone,
                        disabledContentColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(52.dp)
                        .testTag("spin_button"),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSpinning) {
                            CircularProgressIndicator(
                                color = AztecGold,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (!isAffordable) "เงินทุนไม่พอ" else "SPIN (เบท: ${currentBet.roundToInt()})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (!isAffordable) Color.White.copy(alpha = 0.4f) else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun isCellWinning(col: Int, row: Int, winningLines: List<Int>): Boolean {
    for (line in winningLines) {
        when (line) {
            0 -> if (row == 0) return true
            1 -> if (row == 1) return true
            2 -> if (row == 2) return true
            3 -> if (col == row) return true
            4 -> if (col == 0 && row == 2 || col == 1 && row == 1 || col == 2 && row == 0) return true
        }
    }
    return false
}
