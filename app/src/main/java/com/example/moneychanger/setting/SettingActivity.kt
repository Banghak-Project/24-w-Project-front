package com.example.moneychanger.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivitySettingBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.onboarding.LoginActivity
import com.example.moneychanger.onboarding.LoginSelectActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingActivity : BaseActivity() {
    private lateinit var binding: ActivitySettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ TokenManager가 초기화되지 않았다면 강제 초기화
        TokenManager.init(applicationContext)

        val accessToken = TokenManager.getAccessToken()
        Log.d("SettingActivity", "✅ accessToken = $accessToken")

        // 이후에 getUserInfo 호출
        fetchUserInfo()

        binding.loginToolbar.pageText.text = "프로필 수정"

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.login_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val backButton: ImageView = toolbar.findViewById(R.id.button_back)
        backButton.setOnClickListener { finish() }

        binding.buttonEdit.setOnClickListener {
            val intent = Intent(this, EditInfoActivity::class.java)
            startActivity(intent)
        }

        binding.buttonNotice.setOnClickListener {
            val intent = Intent(this, NoticeActivity::class.java)
            startActivity(intent)
        }

        binding.buttonTerm.setOnClickListener {
            val intent = Intent(this, TermActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogout.setOnClickListener { logout() }

        binding.buttonUnsubscribe.setOnClickListener {
            val isKakao = TokenManager.isKakaoUser()

            if (isKakao) {
                // 카카오 유저: 안내 팝업 → 확인 누르면 탈퇴 API 호출
                showUnsubscribePopup()
            } else {
                // 일반 유저: 비밀번호 입력 액티비티로 이동
                val intent = Intent(this, UnsubscribeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // ✅ 사용자 정보 요청
    private fun fetchUserInfo() {
        val accessToken = TokenManager.getAccessToken()
        if (accessToken.isNullOrBlank()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getUserInfo()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val userInfo = response.body()?.data
                        if (userInfo != null) {
                            TokenManager.saveUserInfo(userInfo)
                            updateUserInfo()

                            // 🔥 fetch 완료 후 버튼 리스너 다시 세팅
                            setupUnsubscribeButton()
                        }
                    } else {
                        Toast.makeText(this@SettingActivity, "회원 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupUnsubscribeButton() {
        binding.buttonUnsubscribe.setOnClickListener {
            val isKakao = TokenManager.isKakaoUser()
            Log.d("SettingActivity", "👤 isKakaoUser: $isKakao")

            if (isKakao) {
                // ✅ 카카오 유저 → 안내 팝업 → API 호출
                showKakaoUnsubscribeDialog()
            } else {
                // ✅ 일반 유저 → 비밀번호 입력 액티비티
                val intent = Intent(this, UnsubscribeActivity::class.java)
                startActivity(intent)
            }
        }
    }


    private fun updateUserInfo() {
        val userInfo = TokenManager.getUserInfo()

        if (userInfo != null) {
            binding.textUserName.text = userInfo.userName
            binding.textUserEmail.text = userInfo.userEmail
        } else {
            binding.textUserName.text = "로그인 필요"
            binding.textUserEmail.text = "이메일 없음"
        }
    }

    private fun logout() {
        TokenManager.clearTokens()
        val intent = Intent(this, LoginSelectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showUnsubscribePopup() {
        val dialogView = layoutInflater.inflate(R.layout.unsubscribe_popup, null)
        val dialog = AlertDialog.Builder(this, R.style.PopupDialogTheme)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.button_no).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.button_yes).setOnClickListener {
            val intent = Intent(this, UnsubscribeActivity::class.java)
            startActivity(intent)
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showKakaoUnsubscribeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.unsubscribe_popup, null)
        val dialog = AlertDialog.Builder(this, R.style.PopupDialogTheme)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.button_no).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<TextView>(R.id.button_yes).setOnClickListener {
            performKakaoWithdrawal()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun performKakaoWithdrawal() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.kakaoWithdrawal()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        TokenManager.clearTokens()
                        Toast.makeText(this@SettingActivity, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SettingActivity, UnsubscribeSuccessActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SettingActivity, "탈퇴 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingActivity, "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
