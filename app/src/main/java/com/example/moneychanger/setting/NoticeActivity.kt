package com.example.moneychanger.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.adapter.ExpandableItem
import com.example.moneychanger.adapter.NoticeAdapter
import com.example.moneychanger.databinding.ActivityNoticeBinding
import com.example.moneychanger.etc.BaseActivity

class NoticeActivity : BaseActivity() {
    private lateinit var binding: ActivityNoticeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoticeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginToolbar.pageText.text = "공지사항"

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        // 더미 데이터
        val noticeList = listOf(
            ExpandableItem("공지사항 1", "25/02/01","이것은 공지사항 1의 내용입니다."),
            ExpandableItem("공지사항 2", "25/02/02","이것은 공지사항 2의 내용입니다."),
            ExpandableItem("공지사항 3", "25/02/03","이것은 공지사항 3의 내용입니다."),
            ExpandableItem("공지사항 4", "25/02/04","이것은 공지사항 4의 내용입니다.")
        )

        // recyclerView 연결
        binding.noticeContainer.layoutManager = LinearLayoutManager(this)
        binding.noticeContainer.adapter = NoticeAdapter(noticeList)
    }
}