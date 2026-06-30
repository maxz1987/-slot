package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SessionLog
import com.example.ui.theme.*
import com.example.ui.viewmodel.SlotViewModel
import java.text.SimpleDateFormat
import kotlin.math.roundToInt
import java.util.*

@Composable
fun CoachGuideScreen(
    viewModel: SlotViewModel,
    modifier: Modifier = Modifier
) {
    val allSessions by viewModel.allSessions.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CasinoDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- Header Section ---
        item {
            Text(
                text = "คู่มือเซียนสล็อต & บันทึกสถิติฝึกฝน",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "เรียนรู้ทฤษฎีความน่าจะเป็นของสล็อต และประเมินประวัติการฝึกเล่นจริงเพื่อหาจุดแข็งจุดอ่อนในการบริหารพอร์ตเงิน",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        // --- Educational Guide Accordions ---
        item {
            Text(
                text = "📖 ทฤษฎีสล็อตและการเดินเงินเพื่อคว้าคะแนนสูง",
                color = CasinoNeonGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GuideAccordion(
                    title = "ทำไมการเลือก 'สูตรเดินเงิน' ถึงจำเป็นกว่าหา 'สูตรโกง'?",
                    content = "สล็อตแมชชีนควบคุมด้วยระบบสุ่ม (RNG) ที่ไม่มีใครโกงได้ในระยะยาว ทุกการสปินคือเหตุการณ์อิสระ ความลับในการทำคะแนนเยอะๆ และได้รับแจ็คพอตจึงไม่ใช่การทายสัญลักษณ์ แต่คือ 'การบริหารเงินทุน (Bankroll Management)' เพื่อช่วยให้คุณสามารถอยู่ในเกมให้นานที่สุดจนกว่ารอบโบนัสจะรันมาเจอกัน"
                )

                GuideAccordion(
                    title = "วิเคราะห์เจาะลึก: Martingale vs Proportional",
                    content = "สูตรทบยอดเสียคูณ 2 (Martingale) ออกแบบมาเพื่อคืนทุนรวดเร็วในการชนะเพียงครั้งเดียว แต่จุดอ่อนร้ายแรงคือหากแพ้ติดต่อกัน 7-8 รอบ เบทของคุณจะบานปลายเกินทุนพอร์ตพังทันที (พอร์ตแตก) ส่วนสูตร Proportional (สัดส่วนร้อยละของทุน) จะลดขนาดเดิมพันเมื่อทุนตก ช่วยถนอมพอร์ตให้เล่นได้ยาวนานอย่างต่อเนื่อง มีความปลอดภัยสูงมาก"
                )

                GuideAccordion(
                    title = "ค่าสถิติสำคัญที่ต้องสังเกต: RTP และ Volatility",
                    content = "RTP (Return to Player) คือเปอร์เซ็นต์สะท้อนว่าเครื่องจะจ่ายเงินคืนแก่ผู้เล่นเท่าใดในระยะยาว (ปกติอยู่ที่ 92%-98%) ส่วนความผันผวน (Volatility) บ่งบอกพฤติกรรม ตู้ผันผวนต่ำจะจ่ายชนะบ่อยแต่ยอดเล็ก เหมาะกับการเดินเงินคงที่ ตู้ผันผวนสูงจะแพ้นานๆ แต่หากชนะจะได้แจ็คพอตยักษ์ เหมาะสำหรับการวางเบทแบบค่อยๆ เพิ่มเมื่อขาดทุนสะสม"
                )

                GuideAccordion(
                    title = "กฎเหล็ก 3 ข้อเพื่อเอาชนะความโลภและคว้าชัย",
                    content = "1. ตั้งเป้าหมายกำไร (Take Profit): เมื่อทำคะแนนหรือทุนขึ้นถึงเป้า ให้บันทึกเซสชันและถอนกำไรออกทันที\n2. กำหนดจุดตัดขาดทุน (Stop Loss): หากหมดงบประมาณที่ตั้งไว้ ห้ามเติมเงินทบเพื่อตามทุนเด็ดขาด\n3. ควบคุมเบทเริ่มต้นไม่ให้เกิน 1%-2% ของทุนเสมอเพื่อป้องกันพอร์ตพังระยะสั้น"
                )
            }
        }

        // --- Session Statistics Logs from Room ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏆 บันทึกการฝึกเล่นจริง (${allSessions.size} เซสชัน)",
                    color = CasinoNeonGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (allSessions.isNotEmpty()) {
                    Text(
                        text = "ล้างประวัติทั้งหมด",
                        color = CasinoNeonPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.clearAllHistory() }
                            .padding(4.dp)
                    )
                }
            }
        }

        if (allSessions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CasinoCard.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No sessions",
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ยังไม่มีประวัติการฝึกซ้อม",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "กลับไปเล่นที่แท็บแรก และฝึกซ้อมจนจบเซสชันเพื่อบันทึกประวัติการเดินเงิน",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            items(allSessions, key = { it.id }) { log ->
                SessionLogItem(log = log, onDelete = { viewModel.deleteSessionById(log.id) })
            }
        }
    }
}

@Composable
fun GuideAccordion(
    title: String,
    content: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CasinoCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = CasinoNeonGold,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = content,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SessionLogItem(
    log: SessionLog,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val dateStr = formatter.format(Date(log.timestamp))
    val isWinSession = log.netProfit >= 0.0
    val profitColor = if (isWinSession) CasinoEmerald else CasinoNeonPink
    val profitSign = if (isWinSession) "+" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CasinoCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Date & Strategy & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = dateStr, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    Text(
                        text = "สูตร: ${log.strategyUsed}",
                        color = CasinoNeonGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(8.dp))

            // Body info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "ทุนเริ่มต้น", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    Text(text = "${log.startingBankroll.roundToInt()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text(text = "ทุนจบเกม", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    Text(text = "${log.endingBankroll.roundToInt()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Column {
                    Text(text = "หมุน (Spins)", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    Text(text = "${log.totalSpins}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ผลสุทธิ", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                    Text(
                        text = "$profitSign${log.netProfit.roundToInt()}",
                        color = profitColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
