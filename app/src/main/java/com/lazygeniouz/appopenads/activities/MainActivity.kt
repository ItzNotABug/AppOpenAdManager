package com.lazygeniouz.appopenads.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.lazygeniouz.aoa.listener.AppOpenAdListener
import com.lazygeniouz.appopenads.App
import com.lazygeniouz.appopenads.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Sample App's Main Activity. */
class MainActivity : ComponentActivity() {

    private var adStatus by mutableStateOf(AdStatus.PRELOADING)
    private var events by mutableStateOf<List<String>>(emptyList())
    private val timeFormat by lazy { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    private val app: App
        get() = application as App

    private val adListener = object : AppOpenAdListener() {
        override fun onAdLoaded() {
            if (adStatus != AdStatus.SHOWING) refreshStatus()
            addEvent(getString(R.string.event_ad_loaded))
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            if (adStatus != AdStatus.SHOWING) refreshStatus()
            addEvent(getString(R.string.event_ad_failed_to_load, loadAdError.message))
        }

        override fun onAdWillShow() {
            addEvent(getString(R.string.event_ad_will_show))
        }

        override fun onAdShown() {
            adStatus = AdStatus.SHOWING
            addEvent(getString(R.string.event_ad_shown))
        }

        override fun onAdDismissed() {
            refreshStatus()
            addEvent(getString(R.string.event_ad_dismissed))
        }

        override fun onAdShowFailed(error: FullScreenContentError) {
            refreshStatus()
            addEvent(getString(R.string.event_ad_show_failed, error.message))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        app.adManager.setAppOpenAdListener(adListener)
        setContent {
            SampleTheme {
                SampleScreen(
                    adStatus = adStatus,
                    events = events,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        if (adStatus != AdStatus.SHOWING) refreshStatus()
    }

    override fun onDestroy() {
        if (app.adManager.getAdListener() === adListener) {
            app.adManager.setAppOpenAdListener(null)
        }
        super.onDestroy()
    }

    private fun addEvent(message: String) {
        val event = "[${timeFormat.format(Date())}] $message"
        events = (listOf(event) + events).take(MAX_EVENTS)
    }

    private fun refreshStatus() {
        adStatus = if (app.adManager.isAdAvailable()) AdStatus.READY else AdStatus.PRELOADING
    }

    private companion object {
        const val MAX_EVENTS = 30
    }
}

private enum class AdStatus {
    PRELOADING,
    READY,
    SHOWING,
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SampleTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SampleScreen(
    adStatus: AdStatus,
    events: List<String>,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.appopenads_sample),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                subtitle = { Text(stringResource(R.string.sample_subtitle)) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                StatusCard(adStatus)
            }
            item { EventsCard(events) }
        }
    }
}

@Composable
private fun StatusCard(adStatus: AdStatus) {
    val statusText = when (adStatus) {
        AdStatus.PRELOADING -> stringResource(R.string.ad_status_loading)
        AdStatus.READY -> stringResource(R.string.ad_status_ready)
        AdStatus.SHOWING -> stringResource(R.string.ad_status_showing)
    }
    val statusSymbol = when (adStatus) {
        AdStatus.PRELOADING -> "…"
        AdStatus.READY -> "✓"
        AdStatus.SHOWING -> "↑"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        shape = CircleShape,
                    )
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = statusSymbol,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.ad_status_label),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun EventsCard(events: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.events_label),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            if (events.isEmpty()) {
                Text(
                    text = stringResource(R.string.events_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                events.forEachIndexed { index, event ->
                    Text(
                        text = event,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                    if (index != events.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}