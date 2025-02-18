package com.example.moneychanger.list

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.R
import com.example.moneychanger.adapter.DeleteAdapter
import com.example.moneychanger.adapter.ProductAdapter
import com.example.moneychanger.databinding.ActivityDeleteBinding
import com.example.moneychanger.etc.DataProvider
import com.example.moneychanger.network.product.ProductModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DeleteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener {
            finish()
        }

        binding.loginToolbar.pageText.text = "삭제하기"


        // listActivity에서 전달받은 list_id
        val selectedListId = intent.getLongExtra("list_id", 0L)
        // list_id 이용해서 데이터 필터링
        // 이 부분이 사실 똑같은 과정을 각 페이지(list, delete)에서 하는 것
        // 데이터 모델에 parcelable 구현하면 객체를 직접 전달 가능
        // 무엇이 더 좋을지는 생각해봐야 할듯 - 유빈
        // 전자 - 메모리 사용 절감 / db 접근 많음
        // 후자 - 추가 db 조회 필요 없음 / 데이터 크기가 크면 메모리 사용량 증가
        val productList = DataProvider.productDummyModel.filter { it.listId == selectedListId }

        // 아답터 연결
        val adapter = DeleteAdapter(productList.toMutableList(), { selectedItems ->
            // 삭제된 상품 리스트 처리
            // 여기에 db에서 상품 지우는 코드 들어가면 됨
            // selecteItems가 선택된 상품 리스트
            // ex) deleteProductsFromDB(selectedItems)
            Toast.makeText(this, "${selectedItems.size}개 상품이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }, {isChecked ->
            binding.checkboxAll.isChecked = isChecked
        })

        binding.deleteContainer.layoutManager = LinearLayoutManager(this)
        binding.deleteContainer.adapter = adapter

        // 삭제 버튼 이벤트 (화면에서만 상품 삭제)
        binding.buttonDelete.setOnClickListener {
            adapter.deleteSelectedItems()
            binding.checkboxAll.isChecked = false
        }

        // 전체 선택 체크박스 클릭 시
        binding.checkboxAll.setOnCheckedChangeListener { _, isChecked ->
            adapter.selectAllItems(isChecked)
        }
    }
}
