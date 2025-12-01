package com.example.kotlintest2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs
class MyInfoActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var changeProfileText: TextView
    private lateinit var changePasswordText: TextView
    private lateinit var profileImage: ImageView

    // ⭐ 스와이프 제스처 감지기 추가
    private lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val PREFS_NAME = "QcumbeRPrefs"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"

        // ⭐ 스와이프 임계값
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
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

        // ⭐ 제스처 감지기 초기화
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

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

    // ⭐ 터치 이벤트를 제스처 감지기로 전달
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    // ⭐ 스와이프 제스처 리스너
    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            // 수평 스와이프가 수직보다 클 때만
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // 오른쪽 스와이프 (diffX > 0) → 뒤로가기
                    if (diffX > 0) {
                        finish()
                        return true
                    }
                }
            }
            return false
        }
    }

    // ------------------------------
    // 사용자 정보 로드
    // ------------------------------
    private fun loadUserInfo() {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            nameTextView.text = currentUser.displayName ?: "User"
            emailTextView.text = currentUser.email ?: "email"
        }
        else {
            Log.d("MainActivity", "loadUserInfo: currentUser is null")
            Toast.makeText(this, "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show()
            finish()
        }
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