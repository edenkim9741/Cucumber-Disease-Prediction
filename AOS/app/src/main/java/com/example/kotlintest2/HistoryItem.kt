package com.example.kotlintest2

data class HistoryItem(
    val id: Int,
    val imageResId: Int = 0, // 샘플 이미지용
    val imageUri: String? = null, // 실제 촬영 이미지용
    val diseaseName: String,
    val confidence: Int,
    val date: String
)