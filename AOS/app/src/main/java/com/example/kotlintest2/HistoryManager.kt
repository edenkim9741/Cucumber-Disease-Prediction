package com.example.kotlintest2

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HistoryManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("HistoryPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HISTORY = "history_items"
        private const val KEY_NEXT_ID = "next_id"
    }

    // 촬영 기록 추가
    fun addHistoryItem(imageUri: String, diseaseName: String, confidence: Int): HistoryItem {
        val currentItems = getHistoryItems().toMutableList()

        val nextId = prefs.getInt(KEY_NEXT_ID, 1)
        val date = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())

        val newItem = HistoryItem(
            id = nextId,
            imageUri = imageUri,
            diseaseName = diseaseName,
            confidence = confidence,
            date = date
        )

        currentItems.add(0, newItem)
        saveHistoryItems(currentItems)
        prefs.edit().putInt(KEY_NEXT_ID, nextId + 1).apply()

        return newItem
    }

    // 모든 히스토리 가져오기
    fun getHistoryItems(): List<HistoryItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        return gson.fromJson(json, type)
    }

    // URI가 유효한지 확인
    fun isUriValid(uriString: String?): Boolean {
        if (uriString == null) return false

        return try {
            val uri = Uri.parse(uriString)

            // ContentResolver로 URI 존재 여부 확인
            context.contentResolver.openInputStream(uri)?.use {
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    // 유효하지 않은 항목 자동 제거
    fun cleanupInvalidItems(): Int {
        val currentItems = getHistoryItems()
        var removedCount = 0

        val validItems = currentItems.filter { item ->
            // URI가 있는 항목만 검증 (샘플 데이터는 imageResId 사용)
            if (item.imageUri != null) {
                val isValid = isUriValid(item.imageUri)
                if (!isValid) {
                    removedCount++
                }
                isValid
            } else {
                // URI가 없으면 샘플 데이터이므로 유지
                true
            }
        }

        if (removedCount > 0) {
            saveHistoryItems(validItems)
        }

        return removedCount
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