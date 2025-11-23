package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import androidx.core.content.res.ResourcesCompat
import android.text.Html
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class LoginActivity : AppCompatActivity() {

    // Firebase Auth 객체 선언
    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Firebase Auth 인스턴스 초기화
        auth = FirebaseAuth.getInstance()

        // ✅ 1. 로그인 상태 확인: SharedPreferences 대신 Firebase의 현재 사용자를 확인합니다.
        if (auth.currentUser != null) {
            navigateToMainActivity()
            Log.d("LoginActivity", "User is already logged in")
            return
        }

        setContentView(R.layout.activity_login2)

        // 뷰 초기화
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signupText = findViewById(R.id.signupText)

        // 회원가입 텍스트 설정
        setupSignupText()
        // 로고 투톤 색상 적용
        val logoTextView = findViewById<TextView>(R.id.appTitle)
        val logoText = "QcumbeR"
        val spannableString = SpannableString(logoText)

        logoTextView.typeface = ResourcesCompat.getFont(this, R.font.quantico_bold)

        spannableString.setSpan(
            ForegroundColorSpan(getColor(R.color.dark_green)),
            0, 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(getColor(R.color.dark_green)),
            6, 7,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        logoTextView.text = spannableString

        // 로그인 버튼 클릭 리스너
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // ✅ 2. Firebase를 사용한 로그인 수행
                performLogin(email, password)
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSignupText() {
        val fullText = "계정이 없으신가요? 회원가입"
        val spannableString = SpannableString(fullText)

        // "회원가입" 부분에 클릭 이벤트 적용
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // SignUpActivity로 이동
                val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                // 회원가입 텍스트 스타일 설정
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.primary_green)
                ds.isUnderlineText = false // 밑줄 제거
            }
        }

        // "회원가입" 텍스트의 시작과 끝 인덱스
        val startIndex = fullText.indexOf("회원가입")
        val endIndex = startIndex + "회원가입".length

        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        signupText.text = spannableString
        signupText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun performLogin(email: String, password: String) {
        // Firebase Auth를 사용하여 이메일과 비밀번호로 로그인
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    // 로그인 실패, 사용자에게 실패 메시지 표시
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        // ✅ 로그인 시에는 바로 MainActivity로 (가이드는 회원가입 시에만)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 로그인 액티비티는 종료하여 뒤로 가기 시 다시 보이지 않도록 함
    }
}