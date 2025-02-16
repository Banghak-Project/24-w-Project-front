package com.example.moneychanger.etc

import com.example.moneychanger.network.list.ListModel
import com.example.moneychanger.network.product.ProductModel

// 더미 데이터 관리
object DataProvider {
    val listDummyModel = mutableListOf(
        ListModel(1, "Shopping List", "2025-02-16T15:34:48.807059", "somewhere", false),
        ListModel(2, "Grocery List", "2025-02-16T16:00:00.000000", "marketplace", false),
        ListModel(3, "Travel Checklist", "2025-02-16T17:15:30.123456", "airport", false),
        ListModel(4, "Work Items", "2025-02-16T18:45:10.654321", "office", false)
    )

    val productDummyModel = mutableListOf(
        ProductModel(1, 1, "Laptop", 1200.0),
        ProductModel(2, 1, "Smartphone", 800.0),
        ProductModel(3, 1, "Tablet", 500.0),
        ProductModel(4, 2, "Headphones", 150.0),
        ProductModel(5, 2,"Smartwatch", 300.0)
    )

}


