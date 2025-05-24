package com.example.moneychanger.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityNaviContainerBinding
import com.example.moneychanger.calendar.CallendarFragment
import com.example.moneychanger.calendar.DashboardFragment
import com.example.moneychanger.camera.CameraActivity
import com.example.moneychanger.setting.SettingFragment

class NaviContainerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNaviContainerBinding
    private lateinit var addListLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment())
            .commit()

        addListLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                supportFragmentManager.setFragmentResult("camera_done", Bundle())
            }
        }

        //카메라 버튼 연결
        binding.buttonCamera.setOnClickListener {
            // 카메라 api와 연결하여 동작할 내용
            val intent = Intent(this, CameraActivity::class.java)
            addListLauncher.launch(intent)
        }

        // nav 버튼 클릭 시 Fragment 교체
        binding.navList.setOnClickListener {
            updateNavSelection(binding.navList)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
                .commit()
        }
        binding.navFinan.setOnClickListener {
            updateNavSelection(binding.navFinan)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CallendarFragment())
                .commit()
        }
        binding.navDashboard.setOnClickListener {
            updateNavSelection(binding.navDashboard)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, DashboardFragment())
                .commit()
        }
        binding.navMypage.setOnClickListener {
            updateNavSelection(binding.navMypage)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SettingFragment())
                .commit()
        }

    }

    private fun updateNavSelection(selected: View) {
        val navItems = listOf(
            Triple(binding.navList, binding.imgList, binding.textList),
            Triple(binding.navFinan, binding.imgFinan, binding.textFinan),
            Triple(binding.navDashboard, binding.imgDashboard, binding.textDashboard),
            Triple(binding.navMypage, binding.imgMypage, binding.textMypage)
        )

        for ((layout, imageView, textView) in navItems) {
            val colorRes = if (layout == selected) R.color.main else R.color.gray_02
            imageView.setColorFilter(ContextCompat.getColor(this, colorRes), android.graphics.PorterDuff.Mode.SRC_IN)
            textView.setTextColor(ContextCompat.getColor(this, colorRes))
        }
    }

}