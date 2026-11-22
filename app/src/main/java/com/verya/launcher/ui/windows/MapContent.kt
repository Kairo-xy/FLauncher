package com.verya.launcher.ui.windows

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapContent() {
    var searchInput by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val leafletHtml = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                body, html, #map {
                    margin: 0; padding: 0; height: 100%; width: 100%;
                    background: #0d0e15;
                }
                .leaflet-tile {
                    filter: invert(100%) hue-rotate(180deg) brightness(95%) contrast(90%);
                }
                .leaflet-container {
                    background: #0d0e15 !important;
                }
                .leaflet-control-zoom {
                    display: none;
                }
                .custom-marker {
                    background: #73daca;
                    border: 2px solid #ffffff;
                    border-radius: 50%;
                    width: 12px;
                    height: 12px;
                    box-shadow: 0 0 10px #73daca;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', { zoomControl: false }).setView([35.6895, 139.6917], 13);
                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19,
                    attribution: '&copy; OpenStreetMap'
                }).addTo(map);

                var marker = L.circleMarker([35.6895, 139.6917], {
                    radius: 7,
                    fillColor: "#73daca",
                    color: "#ffffff",
                    weight: 2,
                    opacity: 1,
                    fillOpacity: 0.9
                }).addTo(map);

                function setCenter(lat, lon, zoom) {
                    map.setView([lat, lon], zoom || 14);
                    marker.setLatLng([lat, lon]);
                }

                function zoomIn() { map.zoomIn(); }
                function zoomOut() { map.zoomOut(); }

                function searchLocation(query) {
                    fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(query))
                        .then(r => r.json())
                        .then(data => {
                            if (data && data.length > 0) {
                                var lat = parseFloat(data[0].lat);
                                var lon = parseFloat(data[0].lon);
                                setCenter(lat, lon, 13);
                            }
                        })
                        .catch(e => console.error(e));
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberColors.BgDark)
    ) {
        // Search & Controls Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberColors.BgCard)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Search field
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberColors.BgDark)
                    .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = CyberColors.Cyan,
                    modifier = Modifier.size(16.dp)
                )
                BasicTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = CyberColors.TextPrimary,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 11.sp
                    ),
                    cursorBrush = SolidColor(CyberColors.Cyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        val q = searchInput.trim()
                        if (q.isNotEmpty()) {
                            webViewRef?.evaluateJavascript("searchLocation('$q');", null)
                        }
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    decorationBox = { inner ->
                        if (searchInput.isEmpty()) {
                            Text(
                                text = "search destination...",
                                color = CyberColors.TextMuted,
                                fontFamily = CyberTypography.Mono,
                                fontSize = 11.sp
                            )
                        }
                        inner()
                    }
                )
            }

            // Zoom In
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Zoom In",
                tint = CyberColors.Cyan,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { webViewRef?.evaluateJavascript("zoomIn();", null) }
            )

            // Zoom Out
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Zoom Out",
                tint = CyberColors.Blue,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { webViewRef?.evaluateJavascript("zoomOut();", null) }
            )

            // Re-center
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Reset Location",
                tint = CyberColors.Green,
                modifier = Modifier
                    .size(20.dp)
                    .clickable {
                        webViewRef?.evaluateJavascript("setCenter(35.6895, 139.6917, 13);", null)
                    }
            )
        }

        // Map WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL("https://openstreetmap.org", leafletHtml, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
