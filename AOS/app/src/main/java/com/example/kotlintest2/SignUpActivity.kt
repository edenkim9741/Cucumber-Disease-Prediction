package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.text.Html

class SignUpActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // 뷰 초기화
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginText = findViewById(R.id.loginText)

        // 로그인 밑줄 추가
        loginText.text = Html.fromHtml(
            "이미 계정이 있으신가요? <u>로그인</u>",
            Html.FROM_HTML_MODE_LEGACY
        )

        // 회원가입 버튼 클릭
        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            // 입력 검증
            when {
                name.isEmpty() -> {
                    Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                confirmPassword.isEmpty() -> {
                    Toast.makeText(this, "비밀번호 확인을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    performSignUp(name, email, password)
                }
            }
        }

        // 로그인 화면으로 이동
        loginText.setOnClickListener {
            finish() // 현재 액티비티 종료하고 로그인 화면으로 돌아가기
        }
    }

    private fun performSignUp(name: String, email: String, password: String) {
// 임시로 회원가입 성공 처리
// 추후 Firebase Authentication으로 교체 예정
        Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()

        // 로그인 화면으로 이동
        finish()
    }
}