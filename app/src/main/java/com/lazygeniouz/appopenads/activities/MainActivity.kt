package com.lazygeniouz.appopenads.activities

import android.app.Activity
import android.os.Bundle
import com.lazygeniouz.appopenads.R

/** Sample App's Main Activity */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }

    override fun onBackPressed() {
        finish()
    }
}