package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.SessionLog
import com.example.data.repository.SessionRepository
import com.example.model.BettingStrategy
import com.example.model.SlotSymbol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SlotViewModel(
    application: Application,
    private val repository: SessionRepository
) : AndroidViewModel(application) {

    // --- Game Play State ---
    var bankroll by mutableStateOf(1000.0)
        private set
    var startingBankroll by mutableStateOf(1000.0)
        private set
    var currentBet by mutableStateOf(10.0)
        private set
    var baseBet by mutableStateOf(10.0)
        private set
    var selectedStrategy by mutableStateOf(BettingStrategy.FLAT)
        private set
    var isSpinning by mutableStateOf(false)
        private set
    var totalSpins by mutableStateOf(0)
        private set
    var totalWinsCount by mutableStateOf(0)
        private set
    var maxSingleWin by mutableStateOf(0.0)
        private set
    var lastWin by mutableStateOf(0.0)
        private set
    var totalBetInvested by mutableStateOf(0.0)
        private set
    var totalPayoutAmount by mutableStateOf(0.0)
        private set

    // 3x3 Grid of Reels
    var reels by mutableStateOf(
        listOf(
            listOf(SlotSymbol.CHERRY, SlotSymbol.LEMON, SlotSymbol.BELL),
            listOf(SlotSymbol.BAR, SlotSymbol.SEVEN, SlotSymbol.DIAMOND),
            listOf(SlotSymbol.WILD, SlotSymbol.CHERRY, SlotSymbol.LEMON)
        )
    )
        private set

    // Winning Paylines in current spin (0 to 4)
    var winningPaylines by mutableStateOf<List<Int>>(emptyList())
        private set

    // --- PG Soft Aztec Cascading States ---
    var currentMultiplier by mutableStateOf(1)
        private set
    var winningPositions by mutableStateOf<Set<Pair<Int, Int>>>(emptySet())
        private set
    var isCascading by mutableStateOf(false)
        private set

    // Strategy & Coach Advisors
    var coachMessage by mutableStateOf("ยินดีต้อนรับสู่ SlotCoach! ปรับเบทพื้นฐานและเลือกกลยุทธ์การเดินเงินที่ต้องการฝึกฝน แล้วกดปุ่ม Spin เพื่อเริ่มได้เลยครับ")
        private set
    var coachAdviceType by mutableStateOf("info") // info, warning, success, alert

    // Consecutive Losses Tracker
    private var consecutiveLosses = 0
    private var fibonacciIndex = 0
    private val fibonacciSequence = listOf(1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610)

    // --- Fast Simulation State ---
    var simSpins by mutableStateOf(1000)
        private set
    var simStartingBalance by mutableStateOf(1000.0)
        private set
    var simBaseBet by mutableStateOf(10.0)
        private set
    var simVolatility by mutableStateOf("Medium") // Low, Medium, High
    var simStrategy by mutableStateOf(BettingStrategy.MARTINGALE)
        private set
    var isSimulating by mutableStateOf(false)
        private set

    // Simulation Results
    var simEndingBalance by mutableStateOf(0.0)
    var simTotalSpinsCompleted by mutableStateOf(0)
    var simMaxWin by mutableStateOf(0.0)
    var simPeakBankroll by mutableStateOf(1000.0)
    var simBankruptcyAt by mutableStateOf<Int?>(null) // Spin count when broke
    var simBankrollHistory by mutableStateOf<List<Double>>(emptyList()) // Sampled history for charting
    var simTotalInvested by mutableStateOf(0.0)
    var simTotalReturned by mutableStateOf(0.0)

    // --- History Database State ---
    val allSessions: StateFlow<List<SessionLog>> = repository.allSessions
        .stateInViewModel(emptyList())

    private fun <T> kotlinx.coroutines.flow.Flow<T>.stateInViewModel(initialValue: T): StateFlow<T> {
        return stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = initialValue
        )
    }

    // Initialize Game Parameters
    fun resetSession(startingCapital: Double = 1000.0, initialBaseBet: Double = 10.0) {
        startingBankroll = startingCapital
        bankroll = startingCapital
        baseBet = initialBaseBet
        currentBet = initialBaseBet
        totalSpins = 0
        totalWinsCount = 0
        maxSingleWin = 0.0
        lastWin = 0.0
        totalBetInvested = 0.0
        totalPayoutAmount = 0.0
        consecutiveLosses = 0
        fibonacciIndex = 0
        winningPaylines = emptyList()
        applyStrategyAdjustment()
        updateCoachMessageAndAdvice()
    }

    fun updateBaseBet(newBet: Double) {
        if (!isSpinning) {
            baseBet = newBet
            applyStrategyAdjustment()
        }
    }

    fun updateStrategy(strategy: BettingStrategy) {
        if (!isSpinning) {
            selectedStrategy = strategy
            consecutiveLosses = 0
            fibonacciIndex = 0
            applyStrategyAdjustment()
            updateCoachMessageAndAdvice()
        }
    }

    // Apply Betting Strategy on next bet size
    private fun applyStrategyAdjustment() {
        if (bankroll <= 0) {
            currentBet = 0.0
            return
        }

        when (selectedStrategy) {
            BettingStrategy.FLAT -> {
                currentBet = baseBet
            }
            BettingStrategy.MARTINGALE -> {
                if (consecutiveLosses == 0) {
                    currentBet = baseBet
                } else {
                    currentBet = baseBet * Math.pow(2.0, consecutiveLosses.toDouble())
                }
            }
            BettingStrategy.DALEMBERT -> {
                currentBet = baseBet + (consecutiveLosses * baseBet)
            }
            BettingStrategy.FIBONACCI -> {
                val index = Math.min(fibonacciIndex, fibonacciSequence.size - 1)
                currentBet = baseBet * fibonacciSequence[index]
            }
            BettingStrategy.PROPORTIONAL -> {
                // Bet 2% of current bankroll, round to multiple of 5, minimum 5.0
                val rawBet = bankroll * 0.02
                currentBet = Math.max(5.0, ((rawBet / 5.0).roundToInt() * 5).toDouble())
            }
        }

        // Limit bet to current bankroll
        if (currentBet > bankroll) {
            currentBet = bankroll
        }
    }

    // Generate random symbol based on Volatility
    private fun getRandomSymbol(volatility: String): SlotSymbol {
        val rand = (0..99).random()
        return when (volatility) {
            "Low" -> {
                // High win rate, lower payouts
                when (rand) {
                    in 0..1 -> SlotSymbol.SEVEN       // 2%
                    in 2..5 -> SlotSymbol.WILD        // 4%
                    in 6..12 -> SlotSymbol.DIAMOND    // 7%
                    in 13..24 -> SlotSymbol.BAR       // 12%
                    in 25..42 -> SlotSymbol.BELL      // 18%
                    in 43..70 -> SlotSymbol.CHERRY    // 28%
                    else -> SlotSymbol.LEMON          // 29%
                }
            }
            "High" -> {
                // Lower win rate, massive payouts potential
                when (rand) {
                    in 0..7 -> SlotSymbol.SEVEN       // 8%
                    in 8..15 -> SlotSymbol.WILD       // 8%
                    in 16..21 -> SlotSymbol.DIAMOND   // 6%
                    in 22..28 -> SlotSymbol.BAR       // 7%
                    in 29..38 -> SlotSymbol.BELL      // 10%
                    in 39..65 -> SlotSymbol.CHERRY    // 27%
                    else -> SlotSymbol.LEMON          // 34%
                }
            }
            else -> { // Medium Volatility
                when (rand) {
                    in 0..4 -> SlotSymbol.SEVEN       // 5%
                    in 5..10 -> SlotSymbol.WILD       // 6%
                    in 11..18 -> SlotSymbol.DIAMOND   // 8%
                    in 19..28 -> SlotSymbol.BAR       // 10%
                    in 29..43 -> SlotSymbol.BELL      // 15%
                    in 44..70 -> SlotSymbol.CHERRY    // 27%
                    else -> SlotSymbol.LEMON          // 29%
                }
            }
        }
    }

    // Check payline result
    private fun checkPayline(s1: SlotSymbol, s2: SlotSymbol, s3: SlotSymbol): Pair<Boolean, SlotSymbol?> {
        if (s1 == s2 && s2 == s3) return Pair(true, s1)

        val list = listOf(s1, s2, s3)
        val wildCount = list.count { it == SlotSymbol.WILD }

        if (wildCount == 3) {
            return Pair(true, SlotSymbol.WILD)
        }

        if (wildCount == 2) {
            val nonWild = list.first { it != SlotSymbol.WILD }
            return Pair(true, nonWild)
        }

        if (wildCount == 1) {
            val nonWilds = list.filter { it != SlotSymbol.WILD }
            if (nonWilds[0] == nonWilds[1]) {
                return Pair(true, nonWilds[0])
            }
        }

        return Pair(false, null)
    }

    // Core Spin Action
    fun spin() {
        if (isSpinning || bankroll < currentBet || currentBet <= 0) return

        isSpinning = true
        currentMultiplier = 1
        winningPositions = emptySet()
        isCascading = false
        winningPaylines = emptyList()

        viewModelScope.launch {
            // Deduct Bet
            val betSize = currentBet
            bankroll -= betSize
            totalBetInvested += betSize
            totalSpins++

            // Simulating spin animation delays
            for (i in 1..8) {
                reels = listOf(
                    listOf(getRandomSymbol("Medium"), getRandomSymbol("Medium"), getRandomSymbol("Medium")),
                    listOf(getRandomSymbol("Medium"), getRandomSymbol("Medium"), getRandomSymbol("Medium")),
                    listOf(getRandomSymbol("Medium"), getRandomSymbol("Medium"), getRandomSymbol("Medium"))
                )
                delay(60)
            }

            // Real final reel result
            var currentReels = listOf(
                listOf(getRandomSymbol("Medium"), getRandomSymbol("Medium"), getRandomSymbol("Medium")),
                listOf(getRandomSymbol("Medium"), getRandomSymbol("Medium"), getRandomSymbol("Medium")),
                listOf(getRandomSymbol("Medium"), getRandomSymbol("Medium"), getRandomSymbol("Medium"))
            )
            reels = currentReels

            var spinTotalWin = 0.0
            var hasMoreWins = true
            val accumulatedPaylines = mutableListOf<Int>()

            while (hasMoreWins) {
                // Calculate wins on currentReels
                // Row 0:
                val p0 = checkPayline(currentReels[0][0], currentReels[1][0], currentReels[2][0])
                // Row 1 (Center):
                val p1 = checkPayline(currentReels[0][1], currentReels[1][1], currentReels[2][1])
                // Row 2:
                val p2 = checkPayline(currentReels[0][2], currentReels[1][2], currentReels[2][2])
                // Diagonal Down:
                val p3 = checkPayline(currentReels[0][0], currentReels[1][1], currentReels[2][2])
                // Diagonal Up:
                val p4 = checkPayline(currentReels[0][2], currentReels[1][1], currentReels[2][0])

                val results = listOf(p0, p1, p2, p3, p4)
                val activeWinningLines = mutableListOf<Int>()
                val roundWinningPositions = mutableSetOf<Pair<Int, Int>>()
                var roundWinBeforeMultiplier = 0.0

                results.forEachIndexed { index, pair ->
                    if (pair.first) {
                        activeWinningLines.add(index)
                        val winSymbol = pair.second ?: SlotSymbol.LEMON
                        roundWinBeforeMultiplier += betSize * (winSymbol.multiplier * 0.4) // Scaled multi per payline

                        // Identify winning positions (col, row)
                        when (index) {
                            0 -> { // Row 0
                                roundWinningPositions.add(Pair(0, 0))
                                roundWinningPositions.add(Pair(1, 0))
                                roundWinningPositions.add(Pair(2, 0))
                            }
                            1 -> { // Row 1
                                roundWinningPositions.add(Pair(0, 1))
                                roundWinningPositions.add(Pair(1, 1))
                                roundWinningPositions.add(Pair(2, 1))
                            }
                            2 -> { // Row 2
                                roundWinningPositions.add(Pair(0, 2))
                                roundWinningPositions.add(Pair(1, 2))
                                roundWinningPositions.add(Pair(2, 2))
                            }
                            3 -> { // Diagonal Down
                                roundWinningPositions.add(Pair(0, 0))
                                roundWinningPositions.add(Pair(1, 1))
                                roundWinningPositions.add(Pair(2, 2))
                            }
                            4 -> { // Diagonal Up
                                roundWinningPositions.add(Pair(0, 2))
                                roundWinningPositions.add(Pair(1, 1))
                                roundWinningPositions.add(Pair(2, 0))
                            }
                        }
                    }
                }

                if (activeWinningLines.isNotEmpty()) {
                    accumulatedPaylines.addAll(activeWinningLines)
                    winningPaylines = accumulatedPaylines.distinct()
                    winningPositions = roundWinningPositions

                    val roundWin = roundWinBeforeMultiplier * currentMultiplier
                    spinTotalWin += roundWin
                    lastWin = (spinTotalWin * 10).roundToInt() / 10.0

                    // Wait to show winning highlights
                    delay(850)

                    isCascading = true

                    // Perform cascade drop-down
                    val newReelsMutable = currentReels.map { it.toMutableList() }.toMutableList()
                    for (col in 0..2) {
                        val keepingSymbols = mutableListOf<SlotSymbol>()
                        for (row in 0..2) {
                            if (!winningPositions.contains(Pair(col, row))) {
                                keepingSymbols.add(currentReels[col][row])
                            }
                        }
                        val neededCount = 3 - keepingSymbols.size
                        val refilledSymbols = mutableListOf<SlotSymbol>()
                        for (k in 0 until neededCount) {
                            refilledSymbols.add(getRandomSymbol("Medium"))
                        }
                        val finishedCol = refilledSymbols + keepingSymbols
                        newReelsMutable[col] = finishedCol.toMutableList()
                    }

                    currentReels = newReelsMutable
                    reels = currentReels
                    winningPositions = emptySet()

                    currentMultiplier++
                    delay(600)
                } else {
                    hasMoreWins = false
                }
            }

            isCascading = false
            val finalWin = (spinTotalWin * 10).roundToInt() / 10.0
            lastWin = finalWin

            if (finalWin > 0) {
                bankroll += finalWin
                totalPayoutAmount += finalWin
                totalWinsCount++
                if (finalWin > maxSingleWin) {
                    maxSingleWin = finalWin
                }
                // Reset loss trackers on win
                consecutiveLosses = 0
                if (selectedStrategy == BettingStrategy.FIBONACCI) {
                    fibonacciIndex = Math.max(0, fibonacciIndex - 2)
                }
            } else {
                // Loss
                consecutiveLosses++
                if (selectedStrategy == BettingStrategy.FIBONACCI) {
                    fibonacciIndex++
                }
            }

            isSpinning = false
            applyStrategyAdjustment()
            updateCoachMessageAndAdvice()

            // If bankrupt, save session automatically
            if (bankroll <= 0) {
                saveSessionLog()
            }
        }
    }

    // Save playing session into Room database
    fun saveSessionLog() {
        val currentBankroll = bankroll
        val profit = currentBankroll - startingBankroll
        val spins = totalSpins
        val maxWin = maxSingleWin
        val strategy = selectedStrategy.nameTh

        viewModelScope.launch(Dispatchers.IO) {
            if (spins > 0) {
                repository.insertSession(
                    SessionLog(
                        startingBankroll = startingBankroll,
                        endingBankroll = currentBankroll,
                        totalSpins = spins,
                        maxSingleWin = maxWin,
                        strategyUsed = strategy,
                        netProfit = profit
                    )
                )
            }
        }
    }

    // Dynamic advice and explanations based on real play events
    private fun updateCoachMessageAndAdvice() {
        val currentRtp = if (totalBetInvested > 0) (totalPayoutAmount / totalBetInvested) * 100.0 else 100.0
        val rtpText = String.format("%.1f", currentRtp)

        if (bankroll <= 0) {
            coachMessage = "💥 พอร์ตแตกเรียบร้อย! นี่เป็นบทเรียนสำคัญ: ระบบเดินเงินแบบทบเบทเมื่อแพ้ เช่น Martingale ต้องการเงินทุนที่ไม่จำกัดเพื่อความปลอดภัย หากเสียติดต่อกันนานๆ พอร์ตจะแตกทันที"
            coachAdviceType = "alert"
            return
        }

        // Active advisory
        when (selectedStrategy) {
            BettingStrategy.FLAT -> {
                if (totalSpins == 0) {
                    coachMessage = "คุณใช้กลยุทธ์ Flat Betting ซึ่งเดิมพันยอดเท่ากันทุกตา นี่เป็นวิธีควบคุมเงินที่มั่นคงและถนอมทุนดีที่สุดในการเล่นระยะยาว"
                    coachAdviceType = "info"
                } else if (lastWin > 0) {
                    coachMessage = "ยินดีด้วยครับ! ได้กำไรมา $lastWin เครดิต การเดิมพันแบบ Flat จะคุมเสี่ยงได้นิ่งมาก แนะนำให้เก็บสะสมยอดและคอยสังเกตผลตอบแทนรวมปัจจุบัน ($rtpText% RTP)"
                    coachAdviceType = "success"
                } else {
                    coachMessage = "ไม่เป็นไรครับ ตาที่เสียเป็นเรื่องปกติของความน่าจะเป็น ใน Flat Betting ทุนของคุณจะลดลงช้าที่สุด ทำให้มีโอกาสหมุนเพื่อรอแจ็คพอตได้นานที่สุด"
                    coachAdviceType = "info"
                }
            }
            BettingStrategy.MARTINGALE -> {
                if (consecutiveLosses >= 4) {
                    val nextBet = baseBet * Math.pow(2.0, consecutiveLosses.toDouble())
                    coachMessage = "⚠️ ระวังเตือนภัย! คุณเสียติดต่อกัน $consecutiveLosses ครั้งแล้ว สูตร Martingale ทำให้เบทพุ่งขึ้นเป็น $nextBet เครดิต (ทบมาบานปลาย) หากเสียต่ออีกพอร์ตของคุณจะรับความเสี่ยงสูงมาก!"
                    coachAdviceType = "alert"
                } else if (lastWin > 0 && consecutiveLosses == 0) {
                    coachMessage = "🎯 ทวงทุนสำเร็จ! ระบบ Martingale ล้างยอดเสียสะสมและปรับเบทกลับมาที่ขั้นต่ำ ($baseBet เครดิต) รักษาผลกำไรนี้ไว้ แนะนำว่าถ้ากำไรถึงเป้าให้ถอนทันที!"
                    coachAdviceType = "success"
                } else {
                    coachMessage = "คุณใช้สูตร Martingale (ทบคูณ 2 ยอดเสีย) ตอนนี้เบทขยับเป็น $currentBet เครดิต จำไว้ว่าสูตรนี้ใช้ได้เฉพาะเวลาชนะครั้งเดียวเพื่อดึงทุนคืนทั้งหมด แต่มีความเสี่ยงสูงมากที่จะหมดตัวรวดเร็ว"
                    coachAdviceType = "warning"
                }
            }
            BettingStrategy.DALEMBERT -> {
                if (consecutiveLosses > 0) {
                    coachMessage = "สูตร D'Alembert ปรับเบทเพิ่มขึ้นทีละ 1 ระดับเป็น $currentBet เครดิต เพื่อค่อยๆ ดึงยอดเสียคืนเมื่อชนะ โดยไม่เสี่ยงบานปลายหนักแบบ Martingale"
                    coachAdviceType = "info"
                } else if (lastWin > 0) {
                    coachMessage = "ชนะแล้ว! เบทลดลงมา 1 สเต็ปเป็น $currentBet เครดิต ช่วยเซฟกำไรเอาไว้ ระบบนี้สมดุลและปลอดภัยสำหรับการฝึกหัดเล่นมาก"
                    coachAdviceType = "success"
                }
            }
            BettingStrategy.FIBONACCI -> {
                if (fibonacciIndex >= 6) {
                    coachMessage = "⚠️ เบททบตามชุดเลข Fibonacci ขยับขึ้นมาสูงแล้ว ($currentBet เครดิต) สูตรนี้แรงกดดันต่ำกว่า Martingale เล็กน้อยเพราะเวลาชนะจะถอยกลับมา 2 ขั้น ไม่ได้เริ่มใหม่ทั้งหมด"
                    coachAdviceType = "warning"
                } else {
                    coachMessage = "สไตล์ Fibonacci: วางเบทตามอนุกรมทองคำ ($currentBet เครดิต) เป็นการทบยอดเสียอย่างมีระเบียบวิจัย เหมาะกับเกมสล็อตที่มีความผันผวนปานกลาง"
                    coachAdviceType = "info"
                }
            }
            BettingStrategy.PROPORTIONAL -> {
                val percentOfPort = (currentBet / bankroll) * 100.0
                val percentText = String.format("%.1f", percentOfPort)
                if (lastWin > 0) {
                    coachMessage = "📈 พอร์ตเติบโต! ขนาดเดิมพันของคุณเพิ่มขึ้นเป็น $currentBet ($percentText% ของพอร์ต) ตามการโตของทุน นี่เป็นระบบการเดินเงินเดียวกับที่กองทุนใช้ในการจำกัดความเสี่ยง!"
                    coachAdviceType = "success"
                } else {
                    coachMessage = "เมื่อพอร์ตลดลง ระบบจำกัดความเสี่ยงลดเบทลงมาที่ $currentBet เครดิต เพื่อเซฟพอร์ตไม่ให้แตกง่ายๆ คุณสามารถเล่นต่อได้เรื่อยๆ โดยไม่มีวันพอร์ตแตกทันที"
                    coachAdviceType = "info"
                }
            }
        }
    }

    // --- High-Speed Simulation Actions ---
    fun updateSimSpins(spins: Int) {
        simSpins = spins
    }

    fun updateSimStartingBalance(bal: Double) {
        simStartingBalance = bal
    }

    fun updateSimBaseBet(bet: Double) {
        simBaseBet = bet
    }

    fun updateSimVolatility(vol: String) {
        simVolatility = vol
    }

    fun updateSimStrategy(strat: BettingStrategy) {
        simStrategy = strat
    }

    fun runHighSpeedSimulation() {
        if (isSimulating) return
        isSimulating = true
        simBankruptcyAt = null

        viewModelScope.launch(Dispatchers.Default) {
            val totalSpinsToRun = simSpins
            val startingCapital = simStartingBalance
            val baseBetSize = simBaseBet
            val volatilitySelected = simVolatility
            val strategySelected = simStrategy

            var balance = startingCapital
            var consecutiveLossCount = 0
            var fibIndex = 0
            var maxWinAmount = 0.0
            var peakCapital = startingCapital
            var totalInvested = 0.0
            var totalReturned = 0.0

            val rawHistory = mutableListOf<Double>()
            rawHistory.add(startingCapital)

            for (spinIndex in 1..totalSpinsToRun) {
                if (balance <= 0.0) {
                    simBankruptcyAt = spinIndex - 1
                    balance = 0.0
                    rawHistory.add(0.0)
                    break
                }

                // Determine bet size
                var betSize = when (strategySelected) {
                    BettingStrategy.FLAT -> baseBetSize
                    BettingStrategy.MARTINGALE -> {
                        if (consecutiveLossCount == 0) baseBetSize
                        else baseBetSize * Math.pow(2.0, consecutiveLossCount.toDouble())
                    }
                    BettingStrategy.DALEMBERT -> {
                        baseBetSize + (consecutiveLossCount * baseBetSize)
                    }
                    BettingStrategy.FIBONACCI -> {
                        val index = Math.min(fibIndex, fibonacciSequence.size - 1)
                        baseBetSize * fibonacciSequence[index]
                    }
                    BettingStrategy.PROPORTIONAL -> {
                        Math.max(5.0, ((balance * 0.02) / 5.0).roundToInt() * 5.0)
                    }
                }

                if (betSize > balance) {
                    betSize = balance
                }

                balance -= betSize
                totalInvested += betSize

                // Spin Result Math
                // Generate symbols for 3 reels (3 positions each)
                val s00 = getRandomSymbolForSim(volatilitySelected)
                val s10 = getRandomSymbolForSim(volatilitySelected)
                val s20 = getRandomSymbolForSim(volatilitySelected)

                val s01 = getRandomSymbolForSim(volatilitySelected)
                val s11 = getRandomSymbolForSim(volatilitySelected)
                val s21 = getRandomSymbolForSim(volatilitySelected)

                val s02 = getRandomSymbolForSim(volatilitySelected)
                val s12 = getRandomSymbolForSim(volatilitySelected)
                val s22 = getRandomSymbolForSim(volatilitySelected)

                // Check 5 paylines
                val p0 = checkPayline(s00, s10, s20)
                val p1 = checkPayline(s01, s11, s21)
                val p2 = checkPayline(s02, s12, s22)
                val p3 = checkPayline(s00, s11, s22)
                val p4 = checkPayline(s02, s11, s20)

                val results = listOf(p0, p1, p2, p3, p4)
                var spinWin = 0.0

                results.forEach { pair ->
                    if (pair.first) {
                        val sym = pair.second ?: SlotSymbol.LEMON
                        spinWin += betSize * (sym.multiplier * 0.4)
                    }
                }

                spinWin = (spinWin * 10).roundToInt() / 10.0

                if (spinWin > 0) {
                    balance += spinWin
                    totalReturned += spinWin
                    if (spinWin > maxWinAmount) {
                        maxWinAmount = spinWin
                    }
                    consecutiveLossCount = 0
                    if (strategySelected == BettingStrategy.FIBONACCI) {
                        fibIndex = Math.max(0, fibIndex - 2)
                    }
                } else {
                    consecutiveLossCount++
                    if (strategySelected == BettingStrategy.FIBONACCI) {
                        fibIndex++
                    }
                }

                if (balance > peakCapital) {
                    peakCapital = balance
                }

                rawHistory.add(balance)
            }

            // Downsample history for chart drawing (Target around 50 - 100 data points to avoid Canvas lag)
            val step = Math.max(1, rawHistory.size / 60)
            val sampledHistory = mutableListOf<Double>()
            for (i in 0 until rawHistory.size step step) {
                sampledHistory.add(rawHistory[i])
            }
            if (rawHistory.isNotEmpty() && sampledHistory.last() != rawHistory.last()) {
                sampledHistory.add(rawHistory.last())
            }

            // Update UI State on Main Thread
            launch(Dispatchers.Main) {
                simEndingBalance = balance
                simTotalSpinsCompleted = if (simBankruptcyAt != null) simBankruptcyAt!! else totalSpinsToRun
                simMaxWin = maxWinAmount
                simPeakBankroll = peakCapital
                simBankrollHistory = sampledHistory
                simTotalInvested = totalInvested
                simTotalReturned = totalReturned
                isSimulating = false
            }
        }
    }

    private fun getRandomSymbolForSim(volatility: String): SlotSymbol {
        val rand = (0..99).random()
        return when (volatility) {
            "Low" -> {
                when (rand) {
                    in 0..1 -> SlotSymbol.SEVEN
                    in 2..5 -> SlotSymbol.WILD
                    in 6..11 -> SlotSymbol.DIAMOND
                    in 12..22 -> SlotSymbol.BAR
                    in 23..40 -> SlotSymbol.BELL
                    in 41..69 -> SlotSymbol.CHERRY
                    else -> SlotSymbol.LEMON
                }
            }
            "High" -> {
                when (rand) {
                    in 0..6 -> SlotSymbol.SEVEN
                    in 7..13 -> SlotSymbol.WILD
                    in 14..19 -> SlotSymbol.DIAMOND
                    in 20..26 -> SlotSymbol.BAR
                    in 27..36 -> SlotSymbol.BELL
                    in 37..62 -> SlotSymbol.CHERRY
                    else -> SlotSymbol.LEMON
                }
            }
            else -> { // Medium
                when (rand) {
                    in 0..3 -> SlotSymbol.SEVEN
                    in 4..8 -> SlotSymbol.WILD
                    in 9..16 -> SlotSymbol.DIAMOND
                    in 17..26 -> SlotSymbol.BAR
                    in 27..41 -> SlotSymbol.BELL
                    in 42..68 -> SlotSymbol.CHERRY
                    else -> SlotSymbol.LEMON
                }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllSessions()
        }
    }

    fun deleteSessionById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSessionById(id)
        }
    }
}

// ViewModel factory for constructor injection
class SlotViewModelFactory(
    private val application: Application,
    private val repository: SessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SlotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SlotViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
