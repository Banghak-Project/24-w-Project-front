package com.example.moneychanger.onboarding

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivityPolicyBinding

// CheckBoxSet 데이터 클래스
data class CheckBoxSet(
    val checkBox: CheckBox,
    val container: View,
    val text: TextView,
    val content: TextView,
    val arrow: ImageView
)

class PolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPolicyBinding
    private lateinit var checkBoxSets: List<CheckBoxSet>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CheckBoxSet 초기화
        checkBoxSets = listOf(
            CheckBoxSet(
                binding.checkboxFirst,
                binding.containerFirst,
                binding.textFirst,
                binding.contentFirst,
                binding.arrowFirst
            ),
            CheckBoxSet(
                binding.checkboxSecond,
                binding.containerSecond,
                binding.textSecond,
                binding.contentSecond,
                binding.arrowSecond
            ),
            CheckBoxSet(
                binding.checkboxThird,
                binding.containerThird,
                binding.textThird,
                binding.contentThird,
                binding.arrowThird
            )
        )

        // 전체 선택 체크박스 클릭 시
        binding.checkboxAll.setOnCheckedChangeListener { _, isChecked ->
            updateCheckBoxState(isChecked)

            val textColor = if (isChecked) {
                ContextCompat.getColor(this, R.color.main)
            } else {
                ContextCompat.getColor(this, R.color.gray_08)
            }
            binding.textAll.setTextColor(textColor)
        }

        // 개별 체크박스 클릭 시
        checkBoxSets.forEach { checkBoxSet ->
            checkBoxSet.checkBox.setOnCheckedChangeListener { _, isChecked ->
                updateIndividualCheckBoxUI(
                    isChecked,
                    checkBoxSet.container,
                    checkBoxSet.text,
                    checkBoxSet.content,
                    checkBoxSet.arrow
                )
                if (binding.checkboxAll.isChecked && !isChecked) {
                    binding.checkboxAll.setOnCheckedChangeListener(null)
                    binding.checkboxAll.isChecked = false
                    binding.checkboxAll.setOnCheckedChangeListener { _, isChecked ->
                        updateCheckBoxState(isChecked)
                    }
                } else if (allCheckBoxesChecked()) {
                    binding.checkboxAll.setOnCheckedChangeListener(null)
                    binding.checkboxAll.isChecked = true
                    binding.checkboxAll.setOnCheckedChangeListener { _, isChecked ->
                        updateCheckBoxState(isChecked)
                    }
                }
                updateButtonState() // 버튼 상태 업데이트
            }
        }

        // 버튼 클릭 리스너
        binding.buttonNext.setOnClickListener {
            if (binding.checkboxFirst.isChecked && binding.checkboxSecond.isChecked) {
                val intent = Intent(this, LoginAuthActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "필수 이용약관에 동의해주세요", Toast.LENGTH_SHORT).show()
                Log.d("PolicyActivity", "toast.")
            }
        }

        // 초기 버튼 상태 업데이트
        updateButtonState()
    }

    private fun updateCheckBoxState(isChecked: Boolean) {
        checkBoxSets.forEach { checkBoxSet ->
            checkBoxSet.checkBox.setOnCheckedChangeListener(null)
            checkBoxSet.checkBox.isChecked = isChecked
            checkBoxSet.checkBox.setOnCheckedChangeListener { _, isChecked ->
                updateIndividualCheckBoxUI(
                    isChecked,
                    checkBoxSet.container,
                    checkBoxSet.text,
                    checkBoxSet.content,
                    checkBoxSet.arrow
                )
                binding.checkboxAll.isChecked = allCheckBoxesChecked()
                updateButtonState() // 버튼 상태 업데이트
            }
            updateIndividualCheckBoxUI(
                isChecked,
                checkBoxSet.container,
                checkBoxSet.text,
                checkBoxSet.content,
                checkBoxSet.arrow
            )
        }
    }

    private fun updateIndividualCheckBoxUI(
        isChecked: Boolean,
        container: View,
        text: TextView,
        content: TextView,
        arrow: ImageView
    ) {
        if (isChecked) {
            container.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.blue_01))
            text.setTextColor(ContextCompat.getColor(this, R.color.main))
            content.setTextColor(ContextCompat.getColor(this, R.color.main))
            arrow.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.main))
        } else {
            container.backgroundTintList = null
            text.setTextColor(ContextCompat.getColor(this, R.color.gray_08))
            content.setTextColor(ContextCompat.getColor(this, R.color.gray_08))
            arrow.imageTintList = null
        }
    }

    private fun allCheckBoxesChecked(): Boolean {
        return checkBoxSets.all { it.checkBox.isChecked }
    }

    private fun updateButtonState() {
        binding.buttonNext.isEnabled = binding.checkboxFirst.isChecked && binding.checkboxSecond.isChecked
    }
}


