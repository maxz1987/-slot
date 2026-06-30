package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BettingStrategy
import com.example.ui.theme.*
import com.example.ui.viewmodel.SlotViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastSimulationScreen(
    viewModel: SlotViewModel,
    modifier: Modifier = Modifier
) {
    val simSpins = viewModel.simSpins
    val simStartingBalance = viewModel.simStartingBalance
    val simBaseBet = viewModel.simBaseBet
    val simVolatility = viewModel.simVolatility
    val simStrategy = viewModel.simStrategy
    val isSimulating = viewModel.isSimulating

    val simEndingBalance = viewModel.simEndingBalance
    val simTotalSpinsCompleted = viewModel.simTotalSpinsCompleted
    val simMaxWin = viewModel.simMaxWin
    val simPeakBankroll = viewModel.simPeakBankroll
    val simBankruptcyAt = viewModel.simBankruptcyAt
    val simBankrollHistory = viewModel.simBankrollHistory
    val simTotalInvested = viewModel.simTotalInvested
    val simTotalReturned = viewModel.simTotalReturned

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        Text(
            text = "ระบบวิเคราะห์ย้อนหลัง & คำนวณความเสี่ยง",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "จำลองการหมุนสปินความเร็วสูงได้ถึง 5,000 รอบในเสี้ยววินาที เพื่อทดสอบผลสัมฤทธิ์และตรวจเช็กว่ากลยุทธ์ของคุณจะต้านความผันผวนของสล็อตได้นานเพียงใดก่อนพอร์ตแตก",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        // --- Configuration Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CasinoCard),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⚙️ ตั้งค่าพารามิเตอร์ทดสอบ",
                    color = CasinoNeonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                // Starting Balance Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("เงินเริ่มต้นพอร์ต:", color = Color.White, fontSize = 12.sp)
                        Text("${simStartingBalance.roundToInt()} เครดิต", color = CasinoNeonGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = simStartingBalance.toFloat(),
                        onValueChange = { viewModel.updateSimStartingBalance(it.roundToInt().toDouble()) },
                        valueRange = 100f..5000f,
                        steps = 49,
                        colors = SliderDefaults.colors(
                            thumbColor = CasinoNeonGold,
                            activeTrackColor = CasinoNeonGold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        enabled = !isSimulating
                    )
                }

                // Base Bet Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("เบทพื้นฐาน (Base Bet):", color = Color.White, fontSize = 12.sp)
                        Text("${simBaseBet.roundToInt()} เครดิต", color = CasinoNeonGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = simBaseBet.toFloat(),
                        onValueChange = { viewModel.updateSimBaseBet(it.roundToInt().toDouble()) },
                        valueRange = 5f..200f,
                        steps = 39,
                        colors = SliderDefaults.colors(
                            thumbColor = CasinoNeonGold,
                            activeTrackColor = CasinoNeonGold,
                            inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        enabled = !isSimulating
                    )
                }

                // Spin Counts Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "จำนวนรอบที่ต้องการสปิน:",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )

                    listOf(100, 1000, 5000).forEach { count ->
                        val isSelected = simSpins == count
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) CasinoNeonGold else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !isSimulating) { viewModel.updateSimSpins(count) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Volatility Selector (ความผันผวน)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "ความผันผวนตู้ (Volatility):",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )

                    listOf("Low", "Medium", "High").forEach { vol ->
                        val isSelected = simVolatility == vol
                        val selectColor = when (vol) {
                            "Low" -> CasinoEmerald
                            "High" -> CasinoNeonPink
                            else -> CasinoNeonGold
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) selectColor else Color.White.copy(alpha = 0.05f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = !isSimulating) { viewModel.updateSimVolatility(vol) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (vol == "Low") "ต่ำ" else if (vol == "High") "สูง" else "กลาง",
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Strategy Selector
                Column {
                    Text("กลยุทธ์การทบเงินที่วิเคราะห์:", color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(BettingStrategy.values().toList()) { strategy ->
                            val isSelected = simStrategy == strategy
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) CasinoNeonPink else Color.White.copy(alpha = 0.05f),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable(enabled = !isSimulating) { viewModel.updateSimStrategy(strategy) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = strategy.nameTh,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Trigger Button
                Button(
                    onClick = { viewModel.runHighSpeedSimulation() },
                    enabled = !isSimulating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CasinoNeonPink,
                        disabledContainerColor = CasinoCard.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simulate_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSimulating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("⚡ เริ่มจำลองความเร็วสูง", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        // --- Chart & Results Section ---
        if (simBankrollHistory.isNotEmpty()) {
            // Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CasinoCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📈 แผนภูมิความเคลื่อนไหวเงินทุน",
                            color = CasinoNeonGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (simBankruptcyAt != null) {
                            Text(
                                text = "💥 พอร์ตแตกในตาที่ $simBankruptcyAt",
                                color = CasinoNeonPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "รอดชีวิตครบถ้วน!",
                                color = CasinoEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Dynamic Custom Canvas Line Graph
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(CasinoDark, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val path = Path()
                            val points = simBankrollHistory
                            if (points.size >= 2) {
                                val maxVal = points.maxOrNull() ?: 1.0
                                val minVal = 0.0
                                val delta = if (maxVal == minVal) 1.0 else maxVal - minVal

                                val width = size.width
                                val height = size.height

                                val stepX = width / (points.size - 1)

                                points.forEachIndexed { index, valY ->
                                    // Invert coordinate because Y goes downward in Android canvas
                                    val pctY = (valY - minVal) / delta
                                    val x = index * stepX
                                    val y = (height - (pctY * height)).toFloat()

                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }

                                // Draw neon area below the path
                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(CasinoNeonPink.copy(alpha = 0.15f), Color.Transparent)
                                    )
                                )

                                // Draw line path
                                drawPath(
                                    path = path,
                                    color = if (simBankruptcyAt != null) CasinoNeonPink else CasinoEmerald,
                                    style = Stroke(width = 3f)
                                )
                            }
                        }
                    }

                    // --- Numerical Metrics Table ---
                    Text(
                        text = "📊 ตัวชี้วัดและสถิติจากการสปิน $simTotalSpinsCompleted รอบ",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    val rtpValue = if (simTotalInvested > 0) (simTotalReturned / simTotalInvested) * 100.0 else 0.0
                    val rtpText = String.format("%.1f%%", rtpValue)
                    val profitLoss = simEndingBalance - simStartingBalance
                    val profitLossPct = (profitLoss / simStartingBalance) * 100.0
                    val profitColor = if (profitLoss >= 0) CasinoEmerald else CasinoNeonPink

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetricRow(
                            label = "เงินจบเกม (Ending Bankroll)",
                            value = "${String.format("%,.1f", simEndingBalance)} เครดิต",
                            valColor = profitColor
                        )
                        MetricRow(
                            label = "กำไร/ขาดทุนสุทธิ (Net P/L)",
                            value = "${if (profitLoss >= 0) "+" else ""}${String.format("%,.1f", profitLoss)} (${String.format("%.1f", profitLossPct)}%)",
                            valColor = profitColor
                        )
                        MetricRow(
                            label = "ความเสถียรรักษาพอร์ต",
                            value = if (simBankruptcyAt != null) "ล้มเหลว (ล้มละลาย)" else "เสถียร (รอดชีวิต)",
                            valColor = if (simBankruptcyAt != null) CasinoNeonPink else CasinoEmerald
                        )
                        MetricRow(
                            label = "อัตราจ่ายเฉลี่ยเครื่อง (Simulated RTP)",
                            value = rtpText,
                            valColor = CasinoCyan
                        )
                        MetricRow(
                            label = "ยอดสูงเฉียดฟ้า (Peak Bankroll)",
                            value = "${String.format("%,.1f", simPeakBankroll)} เครดิต",
                            valColor = CasinoNeonGold
                        )
                        MetricRow(
                            label = "ชนะสูงสุดรอบเดียว (Max Win)",
                            value = "${String.format("%,.1f", simMaxWin)} เครดิต",
                            valColor = CasinoEmerald
                        )
                    }
                }
            }
        } else {
            // Empty state placeholder
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CasinoCard.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Analysis",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ไม่มีข้อมูลจำลองขณะนี้",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "เลือกตั้งค่าการเดินเงินของคุณด้านบน แล้วกด 'เริ่มจำลองความเร็วสูง' เพื่อสร้างแผนภูมิวิเคราะห์พฤติกรรมของเงินทุนและตรวจหาความเสี่ยงทันที",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    valColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
        Text(text = value, color = valColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
