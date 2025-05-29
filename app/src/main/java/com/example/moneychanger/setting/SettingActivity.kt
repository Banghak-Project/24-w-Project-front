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

        TokenManager.init(applicationContext)

        val accessToken = TokenManager.getAccessToken()
        Log.d("SettingActivity", "accessToken = $accessToken")

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
            val isGoogle = TokenManager.isGoogleUser()


            when {
                isKakao -> showKakaoUnsubscribeDialog()
                isGoogle -> showGoogleUnsubscribeDialog()
                else     -> showUnsubscribePopup()
            }
        }
    }

    private fun fetchUserInfo() {
        val accessToken = TokenManager.getAccessToken()
        if (accessToken.isNullOrBlank()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getUserInfo()

                withContext(Dispatchers.Main) {
                    if (response.code() == 401) {
                        return@withContext
                    }
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val userInfo = response.body()?.data
                        if (userInfo != null) {
                            TokenManager.saveUserInfo(userInfo)
                            updateUserInfo()

                            setupUnsubscribeButton()
                            }
                        } else {
                            Toast.makeText(
                                this@SettingActivity,
                                "회원 정보를 불러올 수 없습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SettingActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }


    private fun setupUnsubscribeButton() {
        binding.buttonUnsubscribe.setOnClickListener {
            val isKakao = TokenManager.isKakaoUser()
            val isGoogle = TokenManager.isGoogleUser()

            when {
                isKakao  -> showKakaoUnsubscribeDialog()
                isGoogle -> showGoogleUnsubscribeDialog()
                else     -> showUnsubscribePopup()
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

    private fun showGoogleUnsubscribeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.unsubscribe_popup, null)
        val dialog = AlertDialog.Builder(this, R.style.PopupDialogTheme)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.button_no).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.button_yes).setOnClickListener {
            performGoogleWithdrawal()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun performGoogleWithdrawal() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.googleWithdrawal()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        TokenManager.clearTokens()
                        Toast.makeText(this@SettingActivity, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        startActivity(
                            Intent(this@SettingActivity, UnsubscribeSuccessActivity::class.java)
                                .apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                        )
                        finish()
                    } else {
                        Toast.makeText(this@SettingActivity, "탈퇴 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingActivity, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
