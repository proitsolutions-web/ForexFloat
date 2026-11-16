package com.forexfloat.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 40, 32, 40)
            setBackgroundColor(0xFF101820.toInt())
        }

        val title = TextView(this).apply {
            text = "ForexFloat"
            textSize = 30f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        val description = TextView(this).apply {
            text = "A floating market ticker that stays visible over your other apps.\\n\\nThis starter build uses simulated prices for testing."
            textSize = 15f
            setTextColor(0xFFCAD3DC.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 18, 0, 28)
        }
        val start = Button(this).apply {
            text = "Start floating ticker"
            setOnClickListener { startTicker() }
        }
        val stop = Button(this).apply {
            text = "Stop floating ticker"
            setOnClickListener {
                stopService(Intent(this@MainActivity, FloatingTickerService::class.java))
                Toast.makeText(this@MainActivity, "Ticker stopped", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(title)
        root.addView(description)
        root.addView(start)
        root.addView(stop)
        setContentView(root)
    }

    private fun startTicker() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            Toast.makeText(this, "Allow ForexFloat to display over other apps, then return and tap Start.", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(this, FloatingTickerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
        Toast.makeText(this, "Floating ticker started", Toast.LENGTH_SHORT).show()
    }
}
