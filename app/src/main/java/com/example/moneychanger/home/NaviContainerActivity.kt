package com.example.moneychanger.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityNaviContainerBinding

class NaviContainerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNaviContainerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNaviContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment())
            .commit()

        // nav 버튼 클릭 시 Fragment 교체
        binding.navList.setOnClickListener {
            updateNavSelection(binding.navList)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment())
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