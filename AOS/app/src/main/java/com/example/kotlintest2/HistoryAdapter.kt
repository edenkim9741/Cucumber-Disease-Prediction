package com.example.kotlintest2

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var historyItems: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_DATE = 0
        const val VIEW_TYPE_IMAGE = 1
    }

    private var displayItems: List<Any> = emptyList()

    init {
        updateDisplayItems()
    }

    fun updateData(newItems: List<HistoryItem>) {
        historyItems = newItems
        updateDisplayItems()
        notifyDataSetChanged()
    }

    private fun updateDisplayItems() {
        // 이미 정렬된 상태로 들어오므로, 순서 유지하면서 날짜별로 그룹화
        val grouped = LinkedHashMap<String, MutableList<HistoryItem>>()

        historyItems.forEach { item ->
            grouped.getOrPut(item.date) { mutableListOf() }.add(item)
        }

        // 날짜와 아이템을 순서대로 추가
        displayItems = grouped.flatMap { (date, items) ->
            listOf(date) + items
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayItems[position] is String) VIEW_TYPE_DATE else VIEW_TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_DATE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_date_header, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_history_thumbnail, parent, false)
            ImageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is String -> (holder as DateViewHolder).bind(item)
            is HistoryItem -> (holder as ImageViewHolder).bind(item, onItemClick)
        }
    }

    override fun getItemCount(): Int = displayItems.size

    class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(date: String) {
            dateTextView.text = date
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)

        fun bind(item: HistoryItem, onItemClick: (HistoryItem) -> Unit) {
            // URI가 있으면 URI로 로드, 없으면 리소스 ID로 로드
            if (item.imageUri != null) {
                try {
                    val uri = Uri.parse(item.imageUri)
                    thumbnailImageView.setImageURI(uri)
                } catch (e: Exception) {
                    thumbnailImageView.setImageResource(R.drawable.sample_normal1) // 기본 이미지
                }
            } else {
                thumbnailImageView.setImageResource(item.imageResId)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}