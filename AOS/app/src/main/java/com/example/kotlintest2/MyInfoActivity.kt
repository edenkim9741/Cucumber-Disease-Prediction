package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
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

    // 스와이프 제스처 감지기
    private lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)

        // Firebase 인스턴스 초기화
        auth = FirebaseAuth.getInstance()

        // XML 연결
        nameTextView = findViewById(R.id.nameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        changeProfileText = findViewById(R.id.changeProfileText)
        changePasswordText = findViewById(R.id.changePassword)
        profileImage = findViewById(R.id.profileImage)

        // 제스처 감지기 초기화
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        // 저장된 사용자 정보 불러오기
        loadUserInfo()

        // 프로필 사진 변경
        changeProfileText.setOnClickListener {
            Toast.makeText(this, "프로필 사진 변경 기능 (추후 구현)", Toast.LENGTH_SHORT).show()
        }

        profileImage.setOnClickListener {
            Toast.makeText(this, "프로필 사진 변경 (추후 구현)", Toast.LENGTH_SHORT).show()
        }

        // ▼ [변경됨] 비밀번호 변경 클릭 시 다이얼로그 호출
        changePasswordText.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    // ------------------------------
    // ⭐ 비밀번호 변경 다이얼로그 및 로직
    // ------------------------------
    private fun showChangePasswordDialog() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 다이얼로그 안에 넣을 레이아웃 생성
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        // 새 비밀번호 입력창
        val newPasswordInput = EditText(this)
        newPasswordInput.hint = "새 비밀번호 (6자 이상)"
        newPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(newPasswordInput)

        // 비밀번호 확인 입력창
        val confirmPasswordInput = EditText(this)
        confirmPasswordInput.hint = "비밀번호 확인"
        confirmPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(confirmPasswordInput)

        // 다이얼로그 생성
        AlertDialog.Builder(this)
            .setTitle("비밀번호 변경")
            .setView(layout)
            .setPositiveButton("변경") { _, _ ->
                val newPassword = newPasswordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                // 유효성 검사
                if (newPassword.isEmpty() || newPassword.length < 6) {
                    Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Firebase 비밀번호 업데이트 요청
                currentUser.updatePassword(newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            // 보안상 민감한 작업이라 재로그인이 필요할 수 있음
                            val errorMessage = task.exception?.message ?: "오류 발생"
                            Toast.makeText(this, "변경 실패: $errorMessage", Toast.LENGTH_SHORT).show()

                            // 만약 'RecentLoginRequired' 오류라면 재로그인 유도 필요
                            if (errorMessage.contains("recent login")) {
                                Toast.makeText(this, "보안을 위해 다시 로그인 후 시도해주세요.", Toast.LENGTH_LONG).show()
                                logout()
                            }
                        }
                    }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // 터치 이벤트를 제스처 감지기로 전달
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    // 스와이프 제스처 리스너
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

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        finish()
                        return true
                    }
                }
            }
            return false
        }
    }

    private fun loadUserInfo() {
        // onCreate에서 auth를 초기화했으므로 여기선 바로 사용
        val currentUser = auth.currentUser

        if (currentUser != null) {
            nameTextView.text = currentUser.displayName ?: "User"
            emailTextView.text = currentUser.email ?: "email"
        } else {
            Log.d("MyInfoActivity", "loadUserInfo: currentUser is null")
            Toast.makeText(this, "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun logout() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java) // LoginActivity가 있다고 가정
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}