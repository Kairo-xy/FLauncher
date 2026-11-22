package com.verya.launcher.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verya.launcher.data.WeatherService
import com.verya.launcher.model.WeatherInfo
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography
import kotlinx.coroutines.launch

@Composable
fun WeatherContent(
    initialCity: String = "Tokyo"
) {
    val scope = rememberCoroutineScope()
    var selectedCity by remember { mutableStateOf(WeatherService.PRESET_CITIES.first { it.name == initialCity }) }
    var weatherInfo by remember { mutableStateOf<WeatherInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun loadWeather() {
        isLoading = true
        errorMsg = null
        scope.launch {
            val res = WeatherService.fetchWeather(selectedCity.name, selectedCity.lat, selectedCity.lon)
            res.onSuccess {
                weatherInfo = it
                isLoading = false
            }.onFailure {
                errorMsg = it.message ?: "Failed to load weather"
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedCity) {
        loadWeather()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberColors.BgDark)
            .padding(8.dp)
    ) {
        // City selection chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherService.PRESET_CITIES.forEach { city ->
                val isSelected = city.name == selectedCity.name
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyberColors.Highlight else CyberColors.BgCard)
                        .border(
                            1.dp,
                            if (isSelected) CyberColors.Cyan else CyberColors.GlassBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedCity = city }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = city.name,
                        color = if (isSelected) CyberColors.Cyan else CyberColors.TextSecondary,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = CyberColors.Blue,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { loadWeather() }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyberColors.Cyan, modifier = Modifier.size(28.dp))
            }
        } else if (errorMsg != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ERR: $errorMsg",
                    color = CyberColors.Pink,
                    fontFamily = CyberTypography.Mono,
                    fontSize = 12.sp
                )
            }
        } else if (weatherInfo != null) {
            val info = weatherInfo!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current stats card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberColors.BgCard)
                            .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = info.cityName.uppercase(),
                                    color = CyberColors.Cyan,
                                    fontFamily = CyberTypography.Mono,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = info.conditionDescription,
                                    color = CyberColors.TextPrimary,
                                    fontFamily = CyberTypography.Mono,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "${info.temperature.toInt()}°C",
                                color = CyberColors.Green,
                                fontFamily = CyberTypography.Mono,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "FEELS: ${info.apparentTemperature.toInt()}°C",
                                color = CyberColors.TextMuted,
                                fontFamily = CyberTypography.Mono,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "HUMIDITY: ${info.humidity}%",
                                color = CyberColors.TextMuted,
                                fontFamily = CyberTypography.Mono,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "WIND: ${info.windSpeed} km/h",
                                color = CyberColors.TextMuted,
                                fontFamily = CyberTypography.Mono,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // 24H Hourly Forecast
                item {
                    Text(
                        text = "HOURLY FORECAST (24H)",
                        color = CyberColors.TerminalPrompt,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        info.hourly.forEach { h ->
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberColors.BgCard)
                                    .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = h.timeLabel,
                                    color = CyberColors.TextSecondary,
                                    fontFamily = CyberTypography.Mono,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${h.temperature.toInt()}°",
                                    color = CyberColors.Cyan,
                                    fontFamily = CyberTypography.Mono,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 7-Day Forecast
                item {
                    Text(
                        text = "WEEKLY OUTLOOK (7 DAYS)",
                        color = CyberColors.TerminalPrompt,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                items(info.daily) { d ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberColors.BgCard)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = d.dayLabel,
                            color = CyberColors.Blue,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.size(width = 44.dp, height = 16.dp)
                        )
                        Text(
                            text = d.conditionDescription,
                            color = CyberColors.TextPrimary,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${d.minTemp.toInt()}° / ${d.maxTemp.toInt()}°C",
                            color = CyberColors.Green,
                            fontFamily = CyberTypography.Mono,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
