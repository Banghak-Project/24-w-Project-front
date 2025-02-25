package com.example.moneychanger.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.camera.CameraActivity
import com.example.moneychanger.list.ListActivity
import com.example.moneychanger.adapter.ListAdapter
import com.example.moneychanger.R
import com.example.moneychanger.setting.SettingActivity
import com.example.moneychanger.databinding.ActivityMainBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.etc.DataProvider
import com.example.moneychanger.onboarding.find.FindIdPwActivity
import com.example.moneychanger.onboarding.LoginSelectActivity

class MainActivity : BaseActivity() {
    private lateinit var binding:  ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 카메라 버튼 클릭 이벤트 설정
        binding.buttonCamera.setOnClickListener{
            // 카메라 api와 연결하여 동작할 내용
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        // 더미 데이터 - 임시
        val data = DataProvider.listDummyModel

        // recyclerView 연결
        binding.listContainer.layoutManager = LinearLayoutManager(this)
        binding.listContainer.adapter = ListAdapter(data) { item ->
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra("list_id", item.listId)
            startActivity(intent)
        }


        //팝업 띄우기
        showAccessPopup()

        // 임시 버튼 연결 - 임시
        binding.b1.setOnClickListener{
            // 로그인 선택 페이지로 연결
            val intent = Intent(this, LoginSelectActivity::class.java)
            startActivity(intent)
        }
        binding.b2.setOnClickListener{
            val intent = Intent(this, FindIdPwActivity::class.java)
            startActivity(intent)
        }
//        binding.listPlace.root.setOnClickListener{
//            val intent = Intent(this, ListActivity::class.java)
//            startActivity(intent)
//        }



    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.button_setting -> { // 설정 버튼 클릭 처리
                val intent = Intent(this, SettingActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    //팝업 띄우는 함수
    private fun showAccessPopup() {
        val dialogView = layoutInflater.inflate(R.layout.access_popup, null) // 팝업 레이아웃 inflate
        val dialog = AlertDialog.Builder(this, R.style.PopupDialogTheme) // 팝업 테마 적용
            .setView(dialogView)
            .create()

        // 버튼 클릭 이벤트
        val buttonSubmit = dialogView.findViewById<LinearLayout>(R.id.button_submit)
        buttonSubmit.setOnClickListener {
            dialog.dismiss() // 팝업 닫기
        }

        dialog.show()

        // 팝업 크기 설정
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

}