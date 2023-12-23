package com.stepstreak.dev

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.stepstreak.dev.util.BASE_URL
import dev.hotwire.strada.Bridge
import dev.hotwire.turbo.config.TurboPathConfiguration
import com.stepstreak.dev.util.HOME_URL
import com.stepstreak.dev.util.customUserAgent
import com.stepstreak.dev.util.initDayNightTheme
import com.stepstreak.dev.features.web.WebFragment
import com.stepstreak.dev.features.web.WebHomeFragment
import com.stepstreak.dev.features.web.WebPermissionsFragment
import dev.hotwire.turbo.session.TurboSessionNavHostFragment
import kotlin.reflect.KClass

@Suppress("unused")
class MainSessionNavHostFragment : TurboSessionNavHostFragment() {
    override val sessionName = "main"

    override val startLocation = HOME_URL

    override val registeredActivities: List<KClass<out AppCompatActivity>>
        get() = listOf()

    override val registeredFragments: List<KClass<out Fragment>>
        get() = listOf(
            WebFragment::class,
            WebHomeFragment::class,
            WebPermissionsFragment::class
        )

    override val pathConfigurationLocation: TurboPathConfiguration.Location
        get() = TurboPathConfiguration.Location(
            assetFilePath = "json/configuration.json",
            remoteFileUrl = BASE_URL + "configurations/android.json",
        )

    override fun onSessionCreated() {
        super.onSessionCreated()

        // Configure WebView
        session.webView.settings.userAgentString = session.webView.customUserAgent
        session.webView.initDayNightTheme()

        // Initialize Strada bridge with new WebView instance
        Bridge.initialize(session.webView)
    }
}
