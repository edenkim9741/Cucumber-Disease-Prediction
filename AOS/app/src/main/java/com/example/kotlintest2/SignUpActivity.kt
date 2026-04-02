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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var loginText: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginText = findViewById(R.id.loginText)

        setupLoginText()

        signUpButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            when {
                name.isEmpty() -> Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                email.isEmpty() -> Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
                password.isEmpty() -> Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
                confirmPassword.isEmpty() -> Toast.makeText(this, "비밀번호 확인을 입력해주세요", Toast.LENGTH_SHORT).show()
                password != confirmPassword -> Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                password.length < 6 -> Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다", Toast.LENGTH_SHORT).show()
                else -> performSignUp(name, email, password)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun setupLoginText() {
        val fullText = "이미 계정이 있으신가요? 로그인"
        val spannableString = SpannableString(fullText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) { finish() }
            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SignUpActivity, R.color.primary_green)
                ds.isUnderlineText = false
            }
        }
        val startIndex = fullText.indexOf("로그인")
        spannableString.setSpan(clickableSpan, startIndex, startIndex + "로그인".length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginText.text = spannableString
        loginText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun performSignUp(name: String, email: String, password: String) {
        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name).build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {
                            Toast.makeText(this, "회원가입 성공! ${name}님 환영합니다.", Toast.LENGTH_SHORT).show()
                            // 가이드 없이 바로 MainActivity로
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                } else {
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}