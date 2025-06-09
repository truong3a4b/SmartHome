package com.example.smarthome.model

data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Float,
    val humidity: Int
)
data class Weather(
    val main: String,        // Ví dụ: Rain, Clouds, Clear...
    val description: String, // Ví dụ: light rain, few clouds...
    val icon: String         // Dùng để hiển thị icon thời tiết
)