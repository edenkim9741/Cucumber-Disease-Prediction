package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

class LoginActivity : AppCompatActivity() {

    // Firebase Auth 객체 선언
    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    // 회원가입 버튼을 위한 변수도 추가하는 것이 좋습니다. (UI에 버튼 추가 필요)
    // private lateinit var signUpButton: Button

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

        // TODO: UI에 회원가입 버튼을 추가하고 아래 리스너를 연결하세요.
        /*
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                performSignUp(email, password)
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        */
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

    // 이 함수는 사용자가 없는 경우를 대비해 회원가입 기능으로 추가하는 것을 권장합니다.
    private fun performSignUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 회원가입 성공
                    Toast.makeText(this, "회원가입 성공! 자동으로 로그인됩니다.", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    // 회원가입 실패
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 로그인 액티비티는 종료하여 뒤로 가기 시 다시 보이지 않도록 함
    }
}