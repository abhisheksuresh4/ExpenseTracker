package com.example.weatherawareexpensetracker.network

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: Current
)

data class Current(
    @SerializedName("temp_c")
    val tempC: Double,
    val condition: Condition
)

data class Condition(
    val text: String,
    val icon: String
)
