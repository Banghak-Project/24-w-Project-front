package com.example.moneychanger.setting

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.R
import com.example.moneychanger.adapter.NoticeAdapter
import com.example.moneychanger.databinding.ActivityNoticeBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.network.RetrofitClient.apiService
import com.example.moneychanger.network.notice.NoticeResponseDto
import com.example.moneychanger.network.user.ApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoticeActivity : BaseActivity() {
    private lateinit var binding: ActivityNoticeBinding
    private lateinit var noticeList: List<NoticeResponseDto>
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

        fetchNotice()

    }
    private fun fetchNotice() {
        apiService.getAllNotice().enqueue(object :
            Callback<ApiResponse<List<NoticeResponseDto>>> {

            override fun onResponse(
                call: Call<ApiResponse<List<NoticeResponseDto>>>,
                response: Response<ApiResponse<List<NoticeResponseDto>>>
            ) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    noticeList = response.body()?.data ?: emptyList()
                    Log.d("NoticeActivity", "응답 파싱 성공: $noticeList")

                    // recyclerView 연결
                    binding.noticeContainer.layoutManager = LinearLayoutManager(this@NoticeActivity)
                    binding.noticeContainer.adapter = NoticeAdapter(noticeList)
                } else {
                    Toast.makeText(this@NoticeActivity, "공지사항 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(
                call: Call<ApiResponse<List<NoticeResponseDto>>>,
                t: Throwable
            ) {
                Log.e("NoticeActivity", "API 실패", t)
                Toast.makeText(this@NoticeActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

}