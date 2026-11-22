package com.verya.launcher.model

import android.graphics.drawable.Drawable

data class AppItem(
    val label: String,
    val packageName: String,
    val icon: Drawable? = null
)

/** kind: 0 = regular output, 1 = user command echo, 2 = error, 3 = success/accent, 4 = system info */
data class TermLine(
    val text: String,
    val kind: Int = 0
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long
)

data class WeatherInfo(
    val cityName: String,
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val conditionDescription: String,
    val weatherCode: Int,
    val isDay: Boolean,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)

data class HourlyForecast(
    val timeLabel: String,
    val temperature: Double,
    val weatherCode: Int
)

data class DailyForecast(
    val dayLabel: String,
    val minTemp: Double,
    val maxTemp: Double,
    val weatherCode: Int,
    val conditionDescription: String
)
