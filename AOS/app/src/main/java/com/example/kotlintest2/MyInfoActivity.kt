package com.example.kotlintest2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import kotlin.jvm.java
import kotlin.math.abs

class MyInfoActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var changeProfileText: TextView
    private lateinit var changePasswordText: TextView
    private lateinit var profileImage: ImageView
    private lateinit var deleteAccountText: TextView

    // 스와이프 제스처 감지기
    private lateinit var gestureDetector: GestureDetectorCompat

    private lateinit var auth: FirebaseAuth

    // 프로필 사진 URI 저장용
    private var profileImageUri: Uri? = null

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
        private const val PREFS_NAME = "ProfilePrefs"
        private const val KEY_PROFILE_IMAGE = "profile_image_uri"
    }

    // 갤러리에서 이미지 선택
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            saveProfileImage(it)
        }
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
        deleteAccountText = findViewById(R.id.deleteAccountText)

        // 제스처 감지기 초기화
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())

        // 저장된 사용자 정보 불러오기
        loadUserInfo()

        // 저장된 프로필 사진 불러오기
        loadProfileImage()

        // 프로필 사진 변경 - 텍스트 클릭
        changeProfileText.setOnClickListener {
            showProfileImageOptions()
        }

        // 프로필 사진 변경 - 이미지 클릭
        profileImage.setOnClickListener {
            showProfileImageOptions()
        }

        // 비밀번호 변경
        changePasswordText.setOnClickListener {
            showChangePasswordDialog()
        }

        // 회원 탈퇴
        deleteAccountText.setOnClickListener {
            showDeleteAccountDialog()
        }
    }

    // 프로필 사진 옵션 선택 다이얼로그
    private fun showProfileImageOptions() {
        val options = arrayOf("앨범에서 선택", "기본 이미지로 변경", "취소")

        AlertDialog.Builder(this)
            .setTitle("프로필 사진 변경")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> resetToDefaultImage()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    // 갤러리 열기
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    // 선택한 이미지 저장 및 표시
    private fun saveProfileImage(uri: Uri) {
        try {
            // 앱 전용 디렉토리에 이미지 복사
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "profile_image.jpg")

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            // 저장된 파일 URI
            val savedUri = Uri.fromFile(file)
            profileImageUri = savedUri

            // SharedPreferences에 URI 저장
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit().putString(KEY_PROFILE_IMAGE, savedUri.toString()).apply()

            // 이미지 표시
            displayProfileImage(savedUri)

            Toast.makeText(this, "프로필 사진이 변경되었습니다", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("MyInfoActivity", "이미지 저장 실패", e)
            Toast.makeText(this, "이미지 저장 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 프로필 이미지 불러오기
    private fun loadProfileImage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val uriString = prefs.getString(KEY_PROFILE_IMAGE, null)

        if (uriString != null) {
            val uri = Uri.parse(uriString)
            val file = File(uri.path ?: "")

            if (file.exists()) {
                profileImageUri = uri
                displayProfileImage(uri)
            } else {
                // 파일이 없으면 기본 이미지
                resetToDefaultImage()
            }
        }
    }

    // 이미지 표시
    private fun displayProfileImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .placeholder(R.drawable.profile_circle_background)
            .error(R.drawable.profile_circle_background)
            .into(profileImage)
    }

    // 기본 이미지로 리셋
    private fun resetToDefaultImage() {
        // 저장된 프로필 사진 삭제
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        prefs.edit().remove(KEY_PROFILE_IMAGE).apply()

        // 저장된 파일 삭제
        val file = File(filesDir, "profile_image.jpg")
        if (file.exists()) {
            file.delete()
        }

        profileImageUri = null

        // 기본 이미지 표시
        profileImage.setImageResource(R.drawable.profile_circle_background)

        Toast.makeText(this, "기본 이미지로 변경되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteAccountDialog() {
        AlertDialog.Builder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말로 탈퇴하시겠습니까?\n계정 정보가 즉시 삭제되며 복구할 수 없습니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                performDeleteAccount()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun performDeleteAccount() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            currentUser.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 프로필 사진도 함께 삭제
                        resetToDefaultImage()

                        Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_LONG).show()

                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = task.exception?.message ?: "삭제 실패"

                        if (errorMessage.contains("recent login", ignoreCase = true)) {
                            Toast.makeText(this, "보안을 위해 다시 로그인 후 탈퇴해주세요.", Toast.LENGTH_LONG).show()
                            logout()
                        } else {
                            Toast.makeText(this, "오류 발생: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun showChangePasswordDialog() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        val newPasswordInput = EditText(this)
        newPasswordInput.hint = "새 비밀번호 (6자 이상)"
        newPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(newPasswordInput)

        val confirmPasswordInput = EditText(this)
        confirmPasswordInput.hint = "비밀번호 확인"
        confirmPasswordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        layout.addView(confirmPasswordInput)

        AlertDialog.Builder(this)
            .setTitle("비밀번호 변경")
            .setView(layout)
            .setPositiveButton("변경") { _, _ ->
                val newPassword = newPasswordInput.text.toString().trim()
                val confirmPassword = confirmPasswordInput.text.toString().trim()

                if (newPassword.isEmpty() || newPassword.length < 6) {
                    Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                currentUser.updatePassword(newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            val errorMessage = task.exception?.message ?: "오류 발생"
                            Toast.makeText(this, "변경 실패: $errorMessage", Toast.LENGTH_SHORT).show()

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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

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
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}