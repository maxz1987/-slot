package com.example.model

import androidx.compose.ui.graphics.Color

enum class SlotSymbol(
    val label: String,
    val nameTh: String,
    val multiplier: Double,
    val icon: String,
    val color: Color
) {
    SEVEN("PRINCESS", "สาวถ้ำโบราณ (Aztec Princess)", 50.0, "👸", Color(0xFFFF2E93)),
    WILD("WILD", "WILD สุริยะ (Solar Wild)", 30.0, "⭐", Color(0xFFFFD700)),
    DIAMOND("GOLD_MASK", "หน้ากากทองคำ (Golden Mask)", 25.0, "👺", Color(0xFFFFC400)),
    BAR("JAGUAR", "เสือจากัวร์เทพ (Divine Jaguar)", 15.0, "🐆", Color(0xFFFFA726)),
    BELL("SNAKE", "งูเขียวเทพเจ้า (Ancient Snake)", 10.0, "🐍", Color(0xFF26A69A)),
    CHERRY("MONOLITH", "ศิลาอักขระอารยธรรม", 5.0, "🗿", Color(0xFF42A5F5)),
    LEMON("COIN", "เหรียญมายาสีทอง", 3.0, "🪙", Color(0xFFE6EE9C))
}
