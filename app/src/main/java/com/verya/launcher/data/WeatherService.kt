package com.verya.launcher.data

import com.verya.launcher.model.DailyForecast
import com.verya.launcher.model.HourlyForecast
import com.verya.launcher.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WeatherService {

    data class CityLocation(val name: String, val lat: Double, val lon: Double)

    val PRESET_CITIES = listOf(
        CityLocation("Tokyo", 35.6895, 139.6917),
        CityLocation("New York", 40.7128, -74.0060),
        CityLocation("London", 51.5074, -0.1278),
        CityLocation("Paris", 48.8566, 2.3522),
        CityLocation("Seoul", 37.5665, 126.9780),
        CityLocation("Berlin", 52.5200, 13.4050),
        CityLocation("Singapore", 1.3521, 103.8198),
        CityLocation("San Francisco", 37.7749, -122.4194)
    )

    suspend fun fetchWeather(cityName: String, lat: Double, lon: Double): Result<WeatherInfo> =
        withContext(Dispatchers.IO) {
            try {
                val urlStr = "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=$lat&longitude=$lon" +
                        "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,wind_speed_10m" +
                        "&hourly=temperature_2m,weather_code" +
                        "&daily=weather_code,temperature_2m_max,temperature_2m_min" +
                        "&timezone=auto"

                val url = URL(urlStr)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("User-Agent", "FLauncher/1.0 (Android)")
                }

                if (conn.responseCode != 200) {
                    return@withContext Result.failure(Exception("HTTP ${conn.responseCode}"))
                }

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val jsonStr = reader.use { it.readText() }
                conn.disconnect()

                val root = JSONObject(jsonStr)
                val current = root.getJSONObject("current")
                val temp = current.optDouble("temperature_2m", 20.0)
                val appTemp = current.optDouble("apparent_temperature", temp)
                val humidity = current.optInt("relative_humidity_2m", 50)
                val wind = current.optDouble("wind_speed_10m", 5.0)
                val isDay = current.optInt("is_day", 1) == 1
                val code = current.optInt("weather_code", 0)

                // Hourly forecast (next 24 hours)
                val hourlyObj = root.optJSONObject("hourly")
                val hourlyList = mutableListOf<HourlyForecast>()
                if (hourlyObj != null) {
                    val times = hourlyObj.optJSONArray("time")
                    val temps = hourlyObj.optJSONArray("temperature_2m")
                    val codes = hourlyObj.optJSONArray("weather_code")
                    if (times != null && temps != null) {
                        val count = minOf(times.length(), 24)
                        for (i in 0 until count) {
                            val rawTime = times.optString(i)
                            val hourLabel = if (rawTime.contains("T")) {
                                rawTime.substringAfter("T")
                            } else "$i:00"
                            val hTemp = temps.optDouble(i, temp)
                            val hCode = codes?.optInt(i, code) ?: code
                            hourlyList.add(HourlyForecast(hourLabel, hTemp, hCode))
                        }
                    }
                }

                // Daily forecast (next 7 days)
                val dailyObj = root.optJSONObject("daily")
                val dailyList = mutableListOf<DailyForecast>()
                if (dailyObj != null) {
                    val days = dailyObj.optJSONArray("time")
                    val maxTemps = dailyObj.optJSONArray("temperature_2m_max")
                    val minTemps = dailyObj.optJSONArray("temperature_2m_min")
                    val dCodes = dailyObj.optJSONArray("weather_code")
                    if (days != null && maxTemps != null && minTemps != null) {
                        val count = minOf(days.length(), 7)
                        val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val outFormat = SimpleDateFormat("EEE", Locale.getDefault())
                        for (i in 0 until count) {
                            val rawDate = days.optString(i)
                            val dayLabel = try {
                                val d = inFormat.parse(rawDate)
                                if (d != null) outFormat.format(d) else rawDate
                            } catch (e: Exception) {
                                rawDate
                            }
                            val maxT = maxTemps.optDouble(i, temp)
                            val minT = minTemps.optDouble(i, temp)
                            val dCode = dCodes?.optInt(i, code) ?: code
                            dailyList.add(DailyForecast(dayLabel, minT, maxT, dCode, weatherCodeToString(dCode)))
                        }
                    }
                }

                val info = WeatherInfo(
                    cityName = cityName,
                    temperature = temp,
                    apparentTemperature = appTemp,
                    humidity = humidity,
                    windSpeed = wind,
                    conditionDescription = weatherCodeToString(code),
                    weatherCode = code,
                    isDay = isDay,
                    hourly = hourlyList,
                    daily = dailyList
                )
                Result.success(info)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun weatherCodeToString(code: Int): String = when (code) {
        0 -> "Clear Sky"
        1 -> "Mainly Clear"
        2 -> "Partly Cloudy"
        3 -> "Overcast"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        56, 57 -> "Freezing Drizzle"
        61, 63 -> "Rain"
        65 -> "Heavy Rain"
        71, 73, 75 -> "Snow Fall"
        77 -> "Snow Grains"
        80, 81, 82 -> "Rain Showers"
        85, 86 -> "Snow Showers"
        95 -> "Thunderstorm"
        96, 99 -> "Severe Thunderstorm"
        else -> "Cloudy"
    }
}
