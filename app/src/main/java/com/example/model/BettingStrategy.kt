package com.example.model

enum class BettingStrategy(val id: String, val nameTh: String, val description: String) {
    FLAT("flat", "Flat Betting (คงที่)", "เดิมพันด้วยยอดเท่ากันทุกรอบ ปลอดภัยที่สุดในการควบคุมงบประมาณ"),
    MARTINGALE("martingale", "Martingale (ทบ x2)", "เสียแล้วคูณสอง ชนะแล้วกลับมาเริ่มต้น ช่วยทวงทุนคืนไวแต่เสี่ยงพอร์ตแตก"),
    DALEMBERT("dalembert", "D'Alembert (เพิ่ม/ลดทีละระดับ)", "เสียเพิ่ม 1 ระดับ ชนะลด 1 ระดับ เหมาะสำหรับการเล่นระยะยาวแบบกลางๆ"),
    FIBONACCI("fibonacci", "Fibonacci (ทบตามเลขชุด)", "เสียแล้วทบไปขั้นถัดไป ชนะถอยหลังมา 2 ขั้น ใช้ลำดับตัวเลขลดแรงกดดัน"),
    PROPORTIONAL("proportional", "Proportional (ตาม % พอร์ต)", "เดิมพัน 2% ของเงินทุนปัจจุบัน ปรับขนาดอัตโนมัติตามการเติบโตของทุน")
}
