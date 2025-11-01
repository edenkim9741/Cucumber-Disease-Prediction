package com.example.kotlintest2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        backButton = findViewById(R.id.backButton)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        // 뒤로가기 버튼
        backButton.setOnClickListener {
            finish()
        }

        // 샘플 데이터 생성
        val historyItems = getSampleHistoryData()

        // RecyclerView 설정
        historyAdapter = HistoryAdapter(historyItems) { historyItem ->
            navigateToResult(historyItem)
        }

        historyRecyclerView.apply {
            layoutManager = GridLayoutManager(this@HistoryActivity, 3)
            adapter = historyAdapter
            setHasFixedSize(true)
        }
    }

    private fun getSampleHistoryData(): List<HistoryItem> {
        return listOf(
            HistoryItem(1, R.drawable.sample_normal_1, "정상", 97, "2025.08.17"),
            HistoryItem(2, R.drawable.sample_normal_2, "정상", 93, "2025.08.17"),
            HistoryItem(3, R.drawable.sample_normal_3, "정상", 91, "2025.08.17"),
            HistoryItem(4, R.drawable.sample_abnormal_1, "정상", 97, "2025.08.10"),
            HistoryItem(5, R.drawable.sample_abnormal_2, "노균", 91, "2025.08.10"),
            HistoryItem(6, R.drawable.sample_abnormal_3, "노균", 88, "2025.08.10"),
            HistoryItem(7, R.drawable.sample_abnormal_1, "노균", 96, "2025.08.10"),
            HistoryItem(8, R.drawable.sample_abnormal_2, "흰가루", 91, "2025.08.10"),
            HistoryItem(9, R.drawable.sample_abnormal_3, "흰가루", 87, "2025.08.10")
        )
    }

    private fun navigateToResult(historyItem: HistoryItem) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("imageResId", historyItem.imageResId)
        intent.putExtra("diseaseName", historyItem.diseaseName)
        intent.putExtra("confidence", historyItem.confidence)
        intent.putExtra("fromHistory", true)
        startActivity(intent)
    }
}