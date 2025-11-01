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
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class MyInfoFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var myInfoButton: LinearLayout
    private lateinit var logoutButton: LinearLayout
    private lateinit var viewHistoryButton: LinearLayout

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


        loadUserInfo()

        // 내 정보 확인 버튼
        myInfoButton.setOnClickListener {
            Toast.makeText(context, "내 정보 확인 기능 (추후 구현)", Toast.LENGTH_SHORT).show()
        }

        viewHistoryButton.setOnClickListener {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadUserInfo() {
        val prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString(KEY_USER_EMAIL, "user@example.com") ?: "user@example.com"
        val name = prefs.getString(KEY_USER_NAME, "김오이") ?: "김오이"

        nameTextView.text = name
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