package com.example.kotlintest2

import android.content.Context
import android.content.SharedPreferences

object SimpleModeManager {
    private const val PREFS_NAME = "QcumbeRPrefs"
    private const val KEY_SIMPLE_MODE = "isSimpleMode"

    // 간단모드 켜기/끄기
    fun setSimpleMode(context: Context, isEnabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SIMPLE_MODE, isEnabled).apply()
    }

    // 간단모드 상태 확인
    fun isSimpleMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SIMPLE_MODE, false) // 기본값: false (일반모드)
    }
}