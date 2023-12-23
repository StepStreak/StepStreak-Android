package com.stepstreak.dev.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.stepstreak.dev.R
import dev.hotwire.strada.Strada
import dev.hotwire.turbo.config.Turbo
import dev.hotwire.turbo.config.TurboPathConfigurationProperties
import com.stepstreak.dev.strada.bridgeComponentFactories

val TurboPathConfigurationProperties.description: String?
    get() = get("description")

fun Toolbar.displayBackButtonAsCloseIcon() {
    navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_close)
}
@Suppress("DEPRECATION")
fun WebView.initDayNightTheme() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(settings, WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY)
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            when (isNightModeEnabled(context)) {
                true -> WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
                else -> WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_AUTO)
            }
        }
    }
}

val WebView.customUserAgent: String
    get() {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        Log.d("TurboDemo", "packageInfo: $packageInfo")
        var turboSubstring = Turbo.userAgentSubstring()
        turboSubstring += " - " + packageInfo.versionName

        Log.d("TurboDemo", "turboSubstring: $turboSubstring")
        val stradaSubstring = Strada.userAgentSubstring(bridgeComponentFactories)
        return "$turboSubstring; $stradaSubstring; ${settings.userAgentString}"
    }

private fun isNightModeEnabled(context: Context): Boolean {
    val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return currentNightMode == Configuration.UI_MODE_NIGHT_YES
}
