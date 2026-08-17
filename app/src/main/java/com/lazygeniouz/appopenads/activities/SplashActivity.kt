package com.lazygeniouz.appopenads.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** Opens the sample's main screen from the cold-start splash. */
@SuppressLint("CustomSplashScreen")
class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}