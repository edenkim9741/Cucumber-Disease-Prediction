package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java

class HistoryActivity : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var sortButton: LinearLayout
    private lateinit var sortText: TextView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var filterNormal: Chip
    private lateinit var filterDowny: Chip
    private lateinit var filterPowdery: Chip
    private lateinit var filterOod: Chip

    private lateinit var historyManager: HistoryManager

    private var allHistoryItems: List<HistoryItem> = emptyList()
    private var isNewestFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // 뷰 초기화
        backButton = findViewById(R.id.backButton)
        sortButton = findViewById(R.id.sortButton)
        sortText = findViewById(R.id.sortText)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        filterNormal = findViewById(R.id.filterNormal)
        filterDowny = findViewById(R.id.filterDowny)
        filterPowdery = findViewById(R.id.filterPowdery)
        filterOod = findViewById(R.id.filterOod)

        // HistoryManager 초기화
        historyManager = HistoryManager(this)

        // RecyclerView 설정
        setupRecyclerView()

        // 유효하지 않은 항목 정리 (백그라운드)
        cleanupInvalidItemsInBackground()

        // 버튼 리스너
        backButton.setOnClickListener { finish() }
        sortButton.setOnClickListener { showSortDialog() }

        // 필터 리스너
        filterNormal.setOnCheckedChangeListener { _, _ -> applyFilters() }
        filterDowny.setOnCheckedChangeListener { _, _ -> applyFilters() }
        filterPowdery.setOnCheckedChangeListener { _, _ -> applyFilters() }
        filterOod.setOnCheckedChangeListener { _, _ -> applyFilters() }
    }

    override fun onResume() {
        super.onResume()
        // 화면 복귀 시 데이터 새로고침
        loadHistoryData()
        applyFilters()
    }

    // 백그라운드에서 유효하지 않은 항목 정리
    private fun cleanupInvalidItemsInBackground() {
        lifecycleScope.launch {
            val removedCount = withContext(Dispatchers.IO) {
                historyManager.cleanupInvalidItems()
            }

            // UI 업데이트
            loadHistoryData()
            applyFilters()

            // 삭제된 항목이 있으면 알림
            if (removedCount > 0) {
                Toast.makeText(
                    this@HistoryActivity,
                    "삭제된 사진 ${removedCount}개가 기록에서 제거되었습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadHistoryData() {
        // 실제 촬영 기록 로드
        val realHistory = historyManager.getHistoryItems()

        // 샘플 데이터
        val sampleHistory = getSampleHistoryData()

        // 합치기
        allHistoryItems = realHistory + sampleHistory
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList()) { historyItem ->
            navigateToResult(historyItem)
        }

        val gridLayoutManager = GridLayoutManager(this, 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (historyAdapter.getItemViewType(position)) {
                    HistoryAdapter.VIEW_TYPE_DATE -> 3
                    else -> 1
                }
            }
        }

        historyRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = historyAdapter
            setHasFixedSize(false)
        }
    }

    private fun applyFilters() {
        // 필터링
        val filteredItems = allHistoryItems.filter { item ->
            when {
                item.diseaseName.contains("정상") && filterNormal.isChecked -> true
                item.diseaseName.contains("노균") && filterDowny.isChecked -> true
                item.diseaseName.contains("흰가루") && filterPowdery.isChecked -> true
                (item.diseaseName.contains("ood") ||
                        item.diseaseName.contains("OOD") ||
                        item.diseaseName.contains("알 수 없음")) && filterOod.isChecked -> true
                else -> false
            }
        }

        // 정렬
        val sortedItems = if (isNewestFirst) {
            filteredItems.sortedWith(compareByDescending<HistoryItem> { it.date }.thenByDescending { it.id })
        } else {
            filteredItems.sortedWith(compareBy<HistoryItem> { it.date }.thenBy { it.id })
        }

        historyAdapter.updateData(sortedItems)
    }

    private fun showSortDialog() {
        val options = arrayOf("최신순", "오래된 순")
        val checkedItem = if (isNewestFirst) 0 else 1

        AlertDialog.Builder(this)
            .setTitle("정렬")
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                isNewestFirst = (which == 0)
                sortText.text = options[which]
                applyFilters()
                dialog.dismiss()
            }
            .show()
    }

    private fun getSampleHistoryData(): List<HistoryItem> {
        return listOf(
            HistoryItem(1001, R.drawable.sample_normal1, null, "정상", 97, "2025.08.17"),
            HistoryItem(1002, R.drawable.sample_normal2, null, "정상", 93, "2025.08.17"),
            HistoryItem(1003, R.drawable.sample_normal3, null, "정상", 91, "2025.08.10"),
            HistoryItem(1005, R.drawable.sample_abnormal_n1, null, "노균병", 91, "2025.08.17"),
            HistoryItem(1006, R.drawable.sample_abnormal_n2, null, "노균병", 88, "2025.08.10"),
            HistoryItem(1007, R.drawable.sample_abnormal_n3, null, "노균병", 96, "2025.08.10"),
            HistoryItem(1008, R.drawable.sample_abnormal_w1, null, "흰가루병", 91, "2025.08.09"),
            HistoryItem(1009, R.drawable.sample_abnormal_w2, null, "흰가루병", 91, "2025.08.09")
        )
    }

    private fun navigateToResult(historyItem: HistoryItem) {
        val intent = Intent(this, ResultActivity::class.java)

        if (historyItem.imageUri != null) {
            intent.putExtra("imageUri", historyItem.imageUri)
        } else {
            intent.putExtra("imageResId", historyItem.imageResId)
        }

        intent.putExtra("diseaseName", historyItem.diseaseName)
        intent.putExtra("confidence", historyItem.confidence)
        intent.putExtra("fromHistory", true)
        startActivity(intent)
    }
}