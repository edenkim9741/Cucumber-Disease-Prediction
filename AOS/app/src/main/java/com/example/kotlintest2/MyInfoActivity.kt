package com.example.kotlintest2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MyInfoActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var changeProfileText: TextView
    private lateinit var changePasswordText: TextView
    private lateinit var profileImage: ImageView

    companion object {
        private const val PREFS_NAME = "QcumbeRPrefs"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)

        // ▼ XML 연결
        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        changeProfileText = findViewById(R.id.changeProfileText)
        changePasswordText = findViewById(R.id.changePassword)
        profileImage = findViewById(R.id.profileImage)

        // ▼ 저장된 사용자 정보 불러오기
        loadUserInfo()

        // ▼ 프로필 사진 변경
        changeProfileText.setOnClickListener {
            Toast.makeText(this, "프로필 사진 변경 기능 (추후 구현)", Toast.LENGTH_SHORT).show()
        }

        // ▼ 프로필 이미지 눌러도 동작하게
        profileImage.setOnClickListener {
            Toast.makeText(this, "프로필 사진 변경 (추후 구현)", Toast.LENGTH_SHORT).show()
        }

        // ▼ 비밀번호 변경
        changePasswordText.setOnClickListener {
            Toast.makeText(this, "비밀번호 변경 기능 (추후 구현)", Toast.LENGTH_SHORT).show()
        }
    }

    // ------------------------------
    // 사용자 정보 로드
    // ------------------------------
    private fun loadUserInfo() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_USER_EMAIL, "user@example.com") ?: "user@example.com"
        val name = prefs.getString(KEY_USER_NAME, "사용자") ?: "사용자"

        nameTextView.text = name
        emailTextView.text = email
    }

    // ------------------------------
    // 로그아웃 기능 (이 Activity에서는 메뉴/버튼 없지만 필요 시 사용 가능)
    // ------------------------------
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
        FirebaseAuth.getInstance().signOut()
        Log.d("MainActivity", "로그아웃 되었습니다.")

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
