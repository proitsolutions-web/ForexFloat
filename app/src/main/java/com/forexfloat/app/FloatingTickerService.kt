package com.forexfloat.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale
import kotlin.math.abs

class FloatingTickerService : Service() {
    private lateinit var windowManager: WindowManager
    private var tickerView: View? = null
    private lateinit var params: WindowManager.LayoutParams
    private val handler = Handler(Looper.getMainLooper())
    private var tick = 0
    private var lastX = 0f
    private var lastY = 0f
    private var startX = 0
    private var startY = 0

    private val prices = linkedMapOf(
        "EURUSD" to 1.16482,
        "GBPUSD" to 1.32145,
        "USDJPY" to 154.820,
        "XAUUSD" to 4085.50
    )
    private val changes = linkedMapOf(
        "EURUSD" to 0.00001,
        "GBPUSD" to -0.00002,
        "USDJPY" to 0.003,
        "XAUUSD" to 0.10
    )

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1001, buildNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showTicker()
        handler.post(updateRunnable)
    }

    private fun showTicker() {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 10, 14, 10)
            setBackgroundColor(Color.argb(232, 16, 25, 35))
        }
        val header = TextView(this).apply {
            text = "FOREX FLOAT   •   DEMO"
            textSize = 11f
            setTextColor(Color.LTGRAY)
            setPadding(0, 0, 0, 7)
        }
        panel.addView(header)

        prices.forEach { (symbol, price) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 4, 0, 4)
            }
            val name = TextView(this).apply {
                text = symbol
                textSize = 13f
                setTextColor(Color.WHITE)
                width = dp(76)
            }
            val value = TextView(this).apply {
                text = formatPrice(symbol, price)
                textSize = 15f
                setTextColor(if ((changes[symbol] ?: 0.0) >= 0) Color.rgb(46, 220, 145) else Color.rgb(255, 105, 112))
                tag = "price_$symbol"
            }
            row.addView(name)
            row.addView(value)
            panel.addView(row)
        }
        val hint = TextView(this).apply {
            text = "Drag to move  •  Tap × to close"
            textSize = 10f
            setTextColor(Color.LTGRAY)
            setPadding(0, 7, 0, 0)
        }
        panel.addView(hint)

        params = WindowManager.LayoutParams(
            dp(220), WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12)
            y = dp(120)
        }

        panel.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX
                    lastY = event.rawY
                    startX = params.x
                    startY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (event.rawX - lastX).toInt()
                    params.y = startY + (event.rawY - lastY).toInt()
                    try { windowManager.updateViewLayout(panel, params) } catch (_: Exception) {}
                    true
                }
                else -> true
            }
        }
        tickerView = panel
        windowManager.addView(panel, params)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            tick++
            // DEMO ONLY: small deterministic movements, not real market quotes.
            prices.keys.forEach { symbol ->
                val direction = if ((tick + symbol.length) % 2 == 0) 1 else -1
                val step = when (symbol) {
                    "USDJPY" -> 0.001
                    "XAUUSD" -> 0.05
                    else -> 0.00001
                }
                changes[symbol] = direction * step
                prices[symbol] = (prices[symbol] ?: 0.0) + direction * step
                val text = tickerView?.findViewWithTag<TextView>("price_$symbol")
                text?.text = formatPrice(symbol, prices[symbol] ?: 0.0)
                text?.setTextColor(if (direction > 0) Color.rgb(46, 220, 145) else Color.rgb(255, 105, 112))
            }
            handler.postDelayed(this, 1000L)
        }
    }

    private fun formatPrice(symbol: String, value: Double): String =
        if (symbol == "USDJPY") String.format(Locale.US, "%.3f", value)
        else if (symbol == "XAUUSD") String.format(Locale.US, "%.2f", value)
        else String.format(Locale.US, "%.5f", value)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("forexfloat_service", "ForexFloat ticker", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, "forexfloat_service")
        else @Suppress("DEPRECATION") Notification.Builder(this)
        return builder.setContentTitle("ForexFloat is running")
            .setContentText("Your demo floating ticker is active")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        tickerView?.let { try { windowManager.removeView(it) } catch (_: Exception) {} }
        tickerView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
