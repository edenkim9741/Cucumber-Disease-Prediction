package com.example.kotlintest2

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HistoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("HistoryPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HISTORY = "history_items"
        private const val KEY_NEXT_ID = "next_id"
    }

    // 촬영 기록 추가
    fun addHistoryItem(imageUri: String, diseaseName: String, confidence: Int): HistoryItem {
        val currentItems = getHistoryItems().toMutableList()

        // 다음 ID 가져오기
        val nextId = prefs.getInt(KEY_NEXT_ID, 1)

        // 현재 날짜
        val date = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())

        // 새 아이템 생성
        val newItem = HistoryItem(
            id = nextId,
            imageUri = imageUri,
            diseaseName = diseaseName,
            confidence = confidence,
            date = date
        )

        // 리스트에 추가
        currentItems.add(0, newItem) // 맨 앞에 추가

        // 저장
        saveHistoryItems(currentItems)

        // 다음 ID 증가
        prefs.edit().putInt(KEY_NEXT_ID, nextId + 1).apply()

        return newItem
    }

    // 모든 히스토리 가져오기
    fun getHistoryItems(): List<HistoryItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        return gson.fromJson(json, type)
    }

    // 히스토리 저장
    private fun saveHistoryItems(items: List<HistoryItem>) {
        val json = gson.toJson(items)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    // 특정 아이템 삭제
    fun deleteHistoryItem(id: Int) {
        val currentItems = getHistoryItems().toMutableList()
        currentItems.removeAll { it.id == id }
        saveHistoryItems(currentItems)
    }

    // 전체 히스토리 삭제
    fun clearHistory() {
        prefs.edit()
            .remove(KEY_HISTORY)
            .putInt(KEY_NEXT_ID, 1)
            .apply()
    }
}