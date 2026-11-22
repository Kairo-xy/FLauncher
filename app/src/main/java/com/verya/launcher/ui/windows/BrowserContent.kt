package com.verya.launcher.ui.windows

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.verya.launcher.theme.CyberColors
import com.verya.launcher.theme.CyberTypography

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserContent(
    initialUrl: String = "https://duckduckgo.com"
) {
    val context = LocalContext.current
    var urlText by remember { mutableStateOf(initialUrl) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableIntStateOf(100) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    val bookmarks = listOf(
        "DuckDuckGo" to "https://duckduckgo.com",
        "GitHub" to "https://github.com",
        "Wikipedia" to "https://en.m.wikipedia.org",
        "HackerNews" to "https://news.ycombinator.com",
        "Reddit" to "https://old.reddit.com"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberColors.BgDark)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberColors.BgCard)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Back
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = if (canGoBack) CyberColors.Blue else CyberColors.TextMuted,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(enabled = canGoBack) { webViewRef?.goBack() }
            )

            // Forward
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Forward",
                tint = if (canGoForward) CyberColors.Blue else CyberColors.TextMuted,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(enabled = canGoForward) { webViewRef?.goForward() }
            )

            // Reload
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reload",
                tint = CyberColors.Cyan,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { webViewRef?.reload() }
            )

            // URL input
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberColors.BgDark)
                    .border(1.dp, CyberColors.GlassBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = CyberColors.TextPrimary,
                        fontFamily = CyberTypography.Mono,
                        fontSize = 11.sp
                    ),
                    cursorBrush = SolidColor(CyberColors.Cyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        val trimmed = urlText.trim()
                        val finalUrl = when {
                            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
                            else -> "https://duckduckgo.com/?q=" + Uri.encode(trimmed)
                        }
                        urlText = finalUrl
                        webViewRef?.loadUrl(finalUrl)
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Open in external browser
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open in external browser",
                tint = CyberColors.TextSecondary,
                modifier = Modifier
                    .size(18.dp)
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlText)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
            )
        }

        // Bookmark shortcuts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberColors.BgDark)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            bookmarks.forEach { (name, link) ->
                Text(
                    text = name,
                    color = CyberColors.TextSecondary,
                    fontFamily = CyberTypography.Mono,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clickable {
                            urlText = link
                            webViewRef?.loadUrl(link)
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        // Loading indicator
        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = CyberColors.Cyan,
                trackColor = CyberColors.BgDark
            )
        }

        // WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.userAgentString =
                        settings.userAgentString + " FLauncherDesktop/1.0"

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            if (url != null) urlText = url
                            canGoBack = canGoBack()
                            canGoForward = canGoForward()
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            if (url != null) urlText = url
                            canGoBack = canGoBack()
                            canGoForward = canGoForward()
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            return false
                        }
                    }

                    loadUrl(initialUrl)
                    webViewRef = this
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
