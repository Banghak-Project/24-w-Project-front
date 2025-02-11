package com.example.moneychanger.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivitySettingBinding
import com.example.moneychanger.onboarding.LoginSelectActivity

class SettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginToolbar.pageText.text = "프로필 수정"

        binding.buttonUnsubscribe.setOnClickListener{
            // 회원 탈퇴 팝업 띄우기
            showUnsubscribePopup()
        }
    }

    private fun showUnsubscribePopup() {
        val dialogView = layoutInflater.inflate(R.layout.unsubscribe_popup, null) // 팝업 레이아웃 inflate
        val dialog = AlertDialog.Builder(this, R.style.PopupDialogTheme) // 팝업 테마 적용
            .setView(dialogView)
            .create()

        // 버튼 클릭 이벤트
        val buttonSubmitNo = dialogView.findViewById<TextView>(R.id.button_no)
        buttonSubmitNo.setOnClickListener {
            dialog.dismiss() // 아니오 -> 팝업 닫기
        }
        val buttonSubmitYes = dialogView.findViewById<TextView>(R.id.button_yes)
        buttonSubmitYes.setOnClickListener {
            val intent = Intent(this, PasswordInputActivity::class.java)
            startActivity(intent) // 예 -> 비밀번호 입력 페이지로 이동
        }

        dialog.show()

        // 팝업 크기 설정
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}