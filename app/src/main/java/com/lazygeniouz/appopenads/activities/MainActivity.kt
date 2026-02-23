package com.lazygeniouz.appopenads.activities

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError
import com.lazygeniouz.aoa.listener.AppOpenAdListener
import com.lazygeniouz.appopenads.App
import com.lazygeniouz.appopenads.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Sample App's Main Activity */
class MainActivity : ComponentActivity() {

    private lateinit var adStatusView: TextView
    private lateinit var configInfoView: TextView
    private lateinit var eventsLogView: TextView
    private val events = mutableListOf<String>()

    private val timeFormat by lazy { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    private val app: App
        get() = application as App

    private val adListener = object : AppOpenAdListener() {
        override fun onAdLoaded() {
            displayAdStatus()
            addEvent(getString(R.string.event_ad_loaded))
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            displayAdStatus()
            addEvent(getString(R.string.event_ad_failed_to_load, loadAdError.message))
        }

        override fun onAdWillShow() {
            addEvent(getString(R.string.event_ad_will_show))
        }

        override fun onAdShown() {
            displayAdStatus()
            addEvent(getString(R.string.event_ad_shown))
        }

        override fun onAdDismissed() {
            displayAdStatus()
            addEvent(getString(R.string.event_ad_dismissed))
        }

        override fun onAdShowFailed(error: AdError?) {
            addEvent(
                getString(
                    R.string.event_ad_show_failed,
                    error?.message ?: getString(R.string.unknown_error)
                )
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main)

        adStatusView = findViewById(R.id.ad_status)
        configInfoView = findViewById(R.id.config_info)
        eventsLogView = findViewById(R.id.events_log)

        displayAdStatus()
        displayConfigInfo()
    }

    override fun onStart() {
        super.onStart()
        app.adManager.setAppOpenAdListener(adListener)
    }

    override fun onStop() {
        app.adManager.setAppOpenAdListener(null)
        super.onStop()
    }

    private fun addEvent(message: String) {
        val timestamp = timeFormat.format(Date())
        val event = "[$timestamp] $message"
        events.add(0, event)
        if (events.size > 30) {
            events.removeAt(events.lastIndex)
        }
        eventsLogView.text = events.joinToString("\n")
    }

    private fun displayAdStatus() {
        val adManager = app.adManager
        val isAvailable = adManager.isAdAvailable()

        adStatusView.text = when {
            isAvailable -> getString(R.string.ad_status_ready)
            adManager.getAppOpenAd() == null -> getString(R.string.ad_status_loading)
            else -> getString(R.string.ad_status_unavailable)
        }
    }

    private fun displayConfigInfo() {
        val builder = SpannableStringBuilder()
        val keyColor = ContextCompat.getColor(this, R.color.config_key)
        val valueColor = ContextCompat.getColor(this, R.color.config_value)

        fun addConfigItem(key: String, value: String) {
            val startKey = builder.length
            builder.append(key).append(": ")
            builder.setSpan(
                ForegroundColorSpan(keyColor),
                startKey,
                builder.length - 2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val startValue = builder.length
            builder.append(value).append('\n')
            builder.setSpan(
                ForegroundColorSpan(valueColor),
                startValue,
                builder.length - 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        addConfigItem(
            getString(R.string.config_initial_delay),
            getString(R.string.config_initial_delay_none)
        )
        addConfigItem(
            getString(R.string.config_show_in_activities),
            SplashActivity::class.java.simpleName
        )
        addConfigItem(getString(R.string.config_ad_unit), getString(R.string.config_test_ad_unit))
        addConfigItem(getString(R.string.config_paid_event_listener), getString(R.string.enabled))

        if (builder.isNotEmpty()) {
            builder.delete(builder.length - 1, builder.length)
        }
        configInfoView.text = builder
    }

    override fun onResume() {
        super.onResume()
        displayAdStatus()
    }
}