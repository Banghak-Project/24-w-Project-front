package com.example.moneychanger.network.location

data class GeocodeResponse(
    val results: List<Result>,
    val status: String
)

data class Result(
    val formatted_address: String
)
