package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

enum class MedicineType(
    val displayName: String,
    val defaultUnit: String,
    val description: String
) {
    TABLET("錠剤", "錠", "飲み薬（錠剤）"),
    CAPSULE("カプセル", "カプセル", "飲み薬（カプセル）"),
    POWDER("粉薬・散剤", "包", "粉薬・顆粒"),
    SYRUP("シロップ・水薬", "ml", "液体のお薬"),
    INJECTION("注射", "回", "インスリン等"),
    INHALER("吸入", "吸入", "ぜんそく等の吸入薬"),
    PATCH("貼り薬・湿布", "枚", "経皮吸収パッチ・湿布"),
    EYE_DROP("目薬・点眼", "滴", "点眼薬"),
    OINTMENT("軟膏・塗り薬", "回", "外用クリーム・軟膏"),
    OTHER("その他", "回", "その他のお薬");

    fun getIcon(): ImageVector {
        return when (this) {
            TABLET -> Icons.Filled.Medication
            CAPSULE -> Icons.Filled.LocalPharmacy
            POWDER -> Icons.Filled.LocalPharmacy
            SYRUP -> Icons.Filled.WaterDrop
            INJECTION -> Icons.Filled.Vaccines
            INHALER -> Icons.Filled.Air
            PATCH -> Icons.Filled.Healing
            EYE_DROP -> Icons.Filled.Opacity
            OINTMENT -> Icons.Filled.Biotech
            OTHER -> Icons.Filled.Medication
        }
    }
}
