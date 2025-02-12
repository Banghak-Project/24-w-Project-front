package com.example.moneychanger.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityTermBinding
import com.example.moneychanger.etc.BaseActivity

data class TermSet(
    val title: View,
    val content: View,
    val arrow: ImageView
)

class TermActivity : BaseActivity() {

    private lateinit var binding: ActivityTermBinding
    private lateinit var termSets : List<TermSet>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginToolbar.pageText.text = "약관 및 정책"

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        termSets = listOf(
            TermSet(
                binding.term1Title,
                binding.term1DetailContainer,
                binding.term1Arrow
            ),
            TermSet(
                binding.term2Title,
                binding.term2DetailContainer,
                binding.term2Arrow
            ),
            TermSet(
                binding.term3Title,
                binding.term3DetailContainer,
                binding.term3Arrow
            ),
            TermSet(
                binding.term4Title,
                binding.term4DetailContainer,
                binding.term4Arrow
            ),
        )

        setupTermListeners()
    }

    // 클릭 시 약관 펼치기/접기
    private fun setupTermListeners() {
        termSets.forEach { term ->
            term.title.setOnClickListener {
                if (term.content.visibility == View.VISIBLE) {
                    term.content.visibility = View.GONE
                    term.arrow.rotation = 270F
                } else {
                    term.content.visibility = View.VISIBLE
                    term.arrow.rotation = 90F
                }
            }
        }
    }

}