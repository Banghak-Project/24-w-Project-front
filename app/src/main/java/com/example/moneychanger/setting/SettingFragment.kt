package com.example.moneychanger.setting

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.moneychanger.R
import com.example.moneychanger.databinding.ActivitySettingBinding
import com.example.moneychanger.databinding.FragmentDashboardBinding
import com.example.moneychanger.databinding.FragmentSettingBinding
import com.example.moneychanger.etc.BaseActivity
import com.example.moneychanger.network.RetrofitClient
import com.example.moneychanger.network.TokenManager
import com.example.moneychanger.onboarding.LoginActivity
import com.example.moneychanger.onboarding.LoginSelectActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingFragment : Fragment() {
    private  var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view:View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext().applicationContext)

        val accessToken = TokenManager.getAccessToken()
        Log.d("SettingActivity", "accessToken = $accessToken")

        fetchUserInfo()

        binding.buttonEdit.setOnClickListener {
            val intent = Intent(requireContext(), EditInfoActivity::class.java)
            startActivity(intent)
        }

        binding.buttonNotice.setOnClickListener {
            val intent = Intent(requireContext(), NoticeActivity::class.java)
            startActivity(intent)
        }

        binding.buttonTerm.setOnClickListener {
            val intent = Intent(requireContext(), TermActivity::class.java)
            startActivity(intent)
        }

        binding.buttonLogout.setOnClickListener {
            logout()
            val intent = Intent(requireContext(), LoginSelectActivity::class.java)
            startActivity(intent)
        }

//        binding.buttonUnsubscribe.setOnClickListener {
//            val isKakao = TokenManager.isKakaoUser()
//
//            if (isKakao) {
//                showUnsubscribePopup()
//            } else {
//                showUnsubscribePopup()
//            }
//        }
        setupUnsubscribeButton()
    }

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

                            setupUnsubscribeButton()
                        }
                    } else {
                        Toast.makeText(requireContext(), "회원 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupUnsubscribeButton() {
        binding.buttonUnsubscribe.setOnClickListener {
            val isKakao  = TokenManager.isKakaoUser()
            val isGoogle = TokenManager.isGoogleUser()
            Log.d("SettingFragment", "isKakao=$isKakao, isGoogle=$isGoogle")

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
        val intent = Intent(requireContext(), LoginSelectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun showUnsubscribePopup() {
        val dialogView = layoutInflater.inflate(R.layout.unsubscribe_popup, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.PopupDialogTheme)
            .setView(dialogView)
            .create()

        dialogView.findViewById<TextView>(R.id.button_no).setOnClickListener {
            dialog.dismiss()
        }
        dialogView.findViewById<TextView>(R.id.button_yes).setOnClickListener {
            val intent = Intent(requireContext(), UnsubscribeActivity::class.java)
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
        val dialog = AlertDialog.Builder(requireContext(), R.style.PopupDialogTheme)
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
    private fun showGoogleUnsubscribeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.unsubscribe_popup, null)
        AlertDialog.Builder(requireContext(), R.style.PopupDialogTheme)
            .setView(dialogView)
            .create().apply {
                dialogView.findViewById<TextView>(R.id.button_yes).setOnClickListener {
                    performGoogleWithdrawal()
                    dismiss()
                }
                dialogView.findViewById<TextView>(R.id.button_no).setOnClickListener { dismiss() }
                show()
                window?.setLayout(
                    (resources.displayMetrics.widthPixels * 0.8).toInt(),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
    }

    private fun performKakaoWithdrawal() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.kakaoWithdrawal()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        TokenManager.clearTokens()
                        Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(requireContext(), UnsubscribeSuccessActivity::class.java)
                        startActivity(intent)
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        Toast.makeText(requireContext(), "탈퇴 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun performGoogleWithdrawal() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = RetrofitClient.apiService.googleWithdrawal()
            withContext(Dispatchers.Main) {
                if (response.isSuccessful && response.body()?.status=="success") {
                    TokenManager.clearTokens()
                    Toast.makeText(requireContext(), "구글 탈퇴 완료", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), UnsubscribeSuccessActivity::class.java))
                } else {
                    Toast.makeText(requireContext(), "탈퇴 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
