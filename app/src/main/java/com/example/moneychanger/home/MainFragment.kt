package com.example.moneychanger.home


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneychanger.R
import com.example.moneychanger.adapter.ListAdapter
import com.example.moneychanger.camera.CameraActivity
import com.example.moneychanger.calendar.CalendarActivity
import com.example.moneychanger.calendar.DashboardActivity
import com.example.moneychanger.databinding.FragmentMainBinding
import com.example.moneychanger.etc.OnStoreNameUpdatedListener
import com.example.moneychanger.etc.SlideNewList
import com.example.moneychanger.list.ListActivity
import com.example.moneychanger.location.LocationActivity
import com.example.moneychanger.network.RetrofitClient.apiService
import com.example.moneychanger.network.currency.CurrencyManager
import com.example.moneychanger.network.currency.CurrencyModel
import com.example.moneychanger.network.currency.CurrencyResponseDto
import com.example.moneychanger.network.currency.CurrencyViewModel
import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.list.ListsResponseDto
import com.example.moneychanger.network.user.ApiResponse
import com.example.moneychanger.onboarding.find.NewPwActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.absoluteValue

class MainFragment : Fragment(), OnStoreNameUpdatedListener {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ListAdapter
    private lateinit var currencyViewModel: CurrencyViewModel
    private var lists: MutableList<ListModel> = mutableListOf()

    private lateinit var addListLauncher: ActivityResultLauncher<Intent>
    private lateinit var editListLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAdd.setOnClickListener {
            val slideNewList = SlideNewList()
            slideNewList.show(parentFragmentManager, slideNewList.tag)
        }

        adapter = ListAdapter(lists) { item ->
            val intent = Intent(requireContext(), ListActivity::class.java)
            intent.putExtra("list_id", item.listId)
            intent.putExtra("currencyIdFrom", item.currencyFrom.currencyId)
            intent.putExtra("currencyIdTo", item.currencyTo.currencyId)
            editListLauncher.launch(intent)
        }
        binding.listContainer.layoutManager = LinearLayoutManager(requireContext())
        binding.listContainer.adapter = adapter

        if (!hasAllPermissions()) {
            showAccessPopup()
        }

        currencyViewModel = ViewModelProvider(this)[CurrencyViewModel::class.java]
        fetchAndStoreCurrencyData {
            fetchListsFromApi()
        }

        binding.b1.setOnClickListener {
            startActivity(Intent(requireContext(), CalendarActivity::class.java))
        }
        binding.b2.setOnClickListener {
            startActivity(Intent(requireContext(), NewPwActivity::class.java))
        }
        binding.b3.setOnClickListener {
            startActivity(Intent(requireContext(), LocationActivity::class.java))
        }
        binding.b4.setOnClickListener {
            startActivity(Intent(requireContext(), DashboardActivity::class.java))
        }

        parentFragmentManager.setFragmentResultListener("requestKey", viewLifecycleOwner) { _, bundle ->
            val listAdded = bundle.getBoolean("listAdded")
            if (listAdded) {
                fetchListsFromApi()
            }
        }

        addListLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchListsFromApi()
            }
        }
        editListLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                fetchListsFromApi()
            }
        }

        parentFragmentManager.setFragmentResultListener("camera_done", viewLifecycleOwner) { _, _ ->
            fetchListsFromApi()
        }

        binding.mainToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.button_delete -> {
                    adapter.toggledeleteMode()
                    Log.d("deletebutton", "clicked")
                    true
                }
                else -> false
            }
        }
    }


    private fun fetchAndStoreCurrencyData(onFinished: () -> Unit) {
        apiService.findAll().enqueue(object : Callback<ApiResponse<List<CurrencyResponseDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<CurrencyResponseDto>>>,
                response: Response<ApiResponse<List<CurrencyResponseDto>>>
            ) {
                if (response.isSuccessful) {
                    val dtoList = response.body()?.data
                    if (dtoList != null) {
                        val currencyList = dtoList.map {
                            CurrencyModel(
                                currencyId = it.currencyId,
                                curUnit = it.curUnit,
                                dealBasR = it.dealBasR.toDoubleOrNull() ?: 0.0,
                                curNm = it.curNm
                            )
                        }
                        CurrencyManager.setCurrencies(currencyList)
                        Log.d("CurrencyManager", "환율 데이터 저장 완료 (${currencyList.size}개)")
                    }
                }
                onFinished()
            }

            override fun onFailure(call: Call<ApiResponse<List<CurrencyResponseDto>>>, t: Throwable) {
                onFinished()
            }
        })
    }

    override fun onStoreNameUpdated(storeName: String) {
        val listId = UUID.randomUUID().mostSignificantBits.absoluteValue
        val newItem = CurrencyManager.getById(1)?.let {
            CurrencyManager.getById(2)?.let { it1 ->
                ListModel(
                    userId = 1,
                    listId = listId,
                    name = storeName,
                    createdAt = getCurrentDateTime(),
                    location = "",
                    deletedYn = false,
                    currencyFrom = it,
                    currencyTo = it1
                )
            }
        }
        if (newItem != null) {
            adapter.addItem(newItem)
        }
    }

    private fun getCurrentDateTime(): String {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()
    }

    private fun hasAllPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        Log.d("PermissionCheck", "CAMERA: " +
                (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED))
        Log.d("PermissionCheck", "STORAGE: " +
                (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))

        return permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun showAccessPopup() {
        val dialogView = layoutInflater.inflate(R.layout.access_popup, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.PopupDialogTheme)
            .setView(dialogView)
            .create()

        val buttonSubmit = dialogView.findViewById<LinearLayout>(R.id.button_submit)
        buttonSubmit.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun fetchListsFromApi() {
        apiService.getAllLists().enqueue(object : Callback<ApiResponse<List<ListsResponseDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<ListsResponseDto>>>,
                response: Response<ApiResponse<List<ListsResponseDto>>>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.status == "success") {
                        val dtoLists = responseBody.data ?: emptyList()
                        val updatedLists = mutableListOf<ListModel>()

                        dtoLists.forEach { dto ->
                            val currencyFrom = CurrencyManager.getById(dto.currencyFromId)
                            val currencyTo = CurrencyManager.getById(dto.currencyToId)

                            if (currencyFrom == null || currencyTo == null) {
                                Log.e("MainFragment", "통화 정보 매핑 실패: from=${dto.currencyFromId}, to=${dto.currencyToId}")
                                return@forEach
                            }

                            if (!dto.deletedYn) {
                                val updatedList = ListModel(
                                    listId = dto.listId,
                                    name = dto.name,
                                    createdAt = dto.createdAt,
                                    location = dto.location,
                                    deletedYn = false,
                                    currencyFrom = currencyFrom,
                                    currencyTo = currencyTo,
                                    userId = dto.userId
                                )
                                updatedLists.add(updatedList)
                            }
                        }

                        Log.d("MainFragment", "리스트 개수: ${updatedLists.size}")
                        requireActivity().runOnUiThread {
                            adapter.updateList(updatedLists)
                        }
                    } else {
                        Log.e("Retrofit", "응답 status 실패: ${responseBody?.message}")
                    }
                } else {
                    Log.e("Retrofit", "응답 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<ListsResponseDto>>>, t: Throwable) {
                Log.e("Retrofit", "API 호출 실패", t)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

