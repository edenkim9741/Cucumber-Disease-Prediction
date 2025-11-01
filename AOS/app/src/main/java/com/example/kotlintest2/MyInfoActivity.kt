package com.example.kotlintest2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


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
        // Firebase에서 로그아웃합니다
        FirebaseAuth.getInstance().signOut()

        Log.d("MainActivity", "로그아웃 되었습니다.")

        // 로그아웃 후 LoginActivity로 이동합니다.
        val intent = Intent(this, LoginActivity::class.java)
        // 기존의 모든 액티비티를 스택에서 제거하고 새로운 액티비티를 시작합니다.
        // 이렇게 하면 사용자가 뒤로가기 버튼으로 이전 화면으로 돌아갈 수 없습니다.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}