package com.example.kotlintest2

data class HistoryItem(
    val id: Int,
    val imageResId: Int, // 샘플 이미지 리소스 ID
    val diseaseName: String,
    val confidence: Int,
    val date: String
)