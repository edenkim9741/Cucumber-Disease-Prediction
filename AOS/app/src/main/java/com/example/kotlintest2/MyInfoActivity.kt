package com.example.kotlintest2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MyInfoActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button
    private lateinit var backButton: Button

    companion object {
        private const val PREFS_NAME = "QcumbeRPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)

        // 뷰 초기화
        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        logoutButton = findViewById(R.id.logoutButton)
        backButton = findViewById(R.id.backButton)

        // 사용자 정보 불러오기
        loadUserInfo()

        // 뒤로가기 버튼
        backButton.setOnClickListener {
            finish()
        }

        // 프로필 수정 버튼
        editProfileButton.setOnClickListener {
            Toast.makeText(this, "프로필 수정 기능 (추후 구현)", Toast.LENGTH_SHORT).show()
        }

        // 비밀번호 변경 버튼
        changePasswordButton.setOnClickListener {
            Toast.makeText(this, "비밀번호 변경 기능 (추후 구현)", Toast.LENGTH_SHORT).show()
        }

        // 로그아웃 버튼
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadUserInfo() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_USER_EMAIL, "user@example.com") ?: "user@example.com"
        val name = prefs.getString(KEY_USER_NAME, "사용자") ?: "사용자"

        nameTextView.text = name
        emailTextView.text = email
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                logout()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun logout() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }

        Toast.makeText(this, "로그아웃되었습니다", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}