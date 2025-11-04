package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.res.ResourcesCompat
import android.text.Html
import com.google.firebase.auth.FirebaseAuth

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

        // ✅ 로그인 상태 확인: Firebase의 현재 사용자를 확인
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

        // 회원가입 밑줄 추가
        signupText.text = Html.fromHtml(
            "계정이 없으신가요? <u>회원가입</u>",
            Html.FROM_HTML_MODE_LEGACY
        )

        // 로그인 버튼 클릭 리스너
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // 입력 검증
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase 로그인 수행
            performLogin(email, password)
        }

        // 회원가입 화면으로 이동
        signupText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 로그인 액티비티는 종료하여 뒤로 가기 시 다시 보이지 않도록 함
    }
}