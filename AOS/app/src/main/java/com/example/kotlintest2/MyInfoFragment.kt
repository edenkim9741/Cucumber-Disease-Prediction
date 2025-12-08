package com.example.kotlintest2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth


class MyInfoFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var myInfoButton: LinearLayout
    private lateinit var logoutButton: LinearLayout
    private lateinit var viewHistoryButton: LinearLayout
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val PREFS_NAME = "QcumbeRPrefs"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_NAME = "userName"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameTextView = view.findViewById(R.id.nameTextView)
        myInfoButton = view.findViewById(R.id.myInfoButton)
        logoutButton = view.findViewById(R.id.logoutButton)
        viewHistoryButton = view.findViewById(R.id.viewHistoryButton)
        auth = FirebaseAuth.getInstance()

        loadUserInfo()

        // 내 정보 확인 버튼
        myInfoButton.setOnClickListener {
            val intent = Intent(requireContext(), MyInfoActivity::class.java)
            startActivity(intent)
        }

        viewHistoryButton.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        // 간단모드 버튼 추가
        val simpleModeButton = view.findViewById<LinearLayout>(R.id.simpleModeButton)
        val simpleModeSwitch = view.findViewById<SwitchCompat>(R.id.simpleModeSwitch)

        // 현재 간단모드 상태 반영
        simpleModeSwitch?.isChecked = SimpleModeManager.isSimpleMode(requireContext())

        // 버튼 클릭 시 토글
        simpleModeButton?.setOnClickListener {
            val newState = !(simpleModeSwitch?.isChecked ?: false)
            simpleModeSwitch?.isChecked = newState
            SimpleModeManager.setSimpleMode(requireContext(), newState)

            Toast.makeText(
                requireContext(),
                if (newState) "간단 모드가 켜졌습니다" else "간단 모드가 꺼졌습니다",
                Toast.LENGTH_SHORT
            ).show()
        }

        // 스위치 직접 클릭 시에도 저장
        simpleModeSwitch?.setOnCheckedChangeListener { _, isChecked ->
            SimpleModeManager.setSimpleMode(requireContext(), isChecked)
        }

        // 로그아웃 버튼
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadUserInfo() {
        // onCreate에서 auth를 초기화했으므로 여기선 바로 사용
        val currentUser = auth.currentUser

        if (currentUser != null) {
            nameTextView.text = currentUser.displayName ?: "User"
        } else {
            Log.d("MyInfoActivity", "loadUserInfo: currentUser is null")
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                FirebaseAuth.getInstance().signOut()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}