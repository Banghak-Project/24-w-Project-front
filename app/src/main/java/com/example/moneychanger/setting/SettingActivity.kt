package com.example.moneychanger.setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivitySettingBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.onboarding.LoginActivity

class SettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginToolbar.pageText.text = "프로필 수정"

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // 툴바에 타이틀 안보이게

        // 뒤로 가기
        val backButton : ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener{
            finish()
        }

        // 사용자 정보 업데이트
        updateUserInfo()

        // 수정 버튼 클릭 이벤트
        binding.buttonEdit.setOnClickListener {
            // 회원 정보 수정 페이지로 이동
            val intent = Intent(this, EditInfoActivity::class.java)
            startActivity(intent)
        }

        // 공지사항
        binding.buttonNotice.setOnClickListener {
            // 회원 정보 수정 페이지로 이동
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        // 약관 및 정책
        binding.buttonTerm.setOnClickListener {
            // 회원 정보 수정 페이지로 이동
            val intent = Intent(this, TermActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃
        binding.buttonLogout.setOnClickListener {
            logout()
        }

        // 회원 탈퇴 버튼 클릭 이벤트
        binding.buttonUnsubscribe.setOnClickListener{
            // 회원 탈퇴 팝업 띄우기
            showUnsubscribePopup()
        }
    }

    private fun updateUserInfo() {
        val userInfo = TokenManager.getUserInfo()

        if (userInfo != null) {
            // ✅ UI 업데이트: 사용자 이름 & 이메일 설정
            binding.textUserName.text = userInfo.userName ?: "사용자"
            binding.textUserEmail.text = userInfo.userEmail ?: "이메일 없음"
        } else {
            binding.textUserName.text = "로그인 필요"
            binding.textUserEmail.text = "이메일 없음"
        }
    }


    private fun logout() {
        TokenManager.clearTokens()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
            val intent = Intent(this, UnsubscribeActivity::class.java)
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