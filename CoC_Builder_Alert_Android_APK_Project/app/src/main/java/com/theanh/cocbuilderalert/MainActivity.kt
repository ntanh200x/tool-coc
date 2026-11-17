package com.theanh.cocbuilderalert

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }

        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                if (url.startsWith("clashofclans://")) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        val launchIntent = packageManager.getLaunchIntentForPackage("com.supercell.clashofclans")
                        if (launchIntent != null) startActivity(launchIntent)
                    }
                    return true
                }
                return false
            }
        }

        webView.addJavascriptInterface(AndroidNativeBridge(this), "AndroidNative")
        webView.loadUrl("file:///android_asset/index.html")
    }

    class AndroidNativeBridge(private val context: Context) {
        @JavascriptInterface
        fun syncTasksJson(jsonArrayStr: String) {
            try {
                val arr = JSONArray(jsonArrayStr)
                val prefs = context.getSharedPreferences("coc_widget_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("tasks_json", jsonArrayStr).apply()

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val now = System.currentTimeMillis()

                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val realRemMs = obj.optLong("realRemainingMs", 0L)
                    if (realRemMs <= 0L) continue

                    val leadMin = obj.optInt("leadMinutes", 5)
                    val alertAtZero = obj.optBoolean("alertAtZero", true)
                    val vibrateOnly = obj.optBoolean("vibrateOnly", false)
                    val soundEnabled = obj.optBoolean("soundEnabled", true)
                    val builder = obj.optString("builder", "Thợ xây")
                    val name = obj.optString("name", "Công trình")
                    val baseId = (obj.optString("id", "t$i").hashCode() and 0x7fffffff) % 50000

                    // 1. Đặt lịch báo trước (3p, 4p, 5p, 10p)
                    val leadMs = leadMin * 60_000L
                    if (leadMin > 0 && realRemMs > leadMs) {
                        scheduleSingleAlarm(
                            alarmManager,
                            baseId + 1,
                            now + (realRemMs - leadMs),
                            "⚒️ $builder sắp xong ($name)!",
                            "Chỉ còn $leadMin phút nữa là nâng cấp xong. Bấm để vào game ngay!",
                            vibrateOnly || !soundEnabled
                        )
                    }

                    // 2. Đặt lịch báo ngay khi chạm 0s
                    if (leadMin == 0 || alertAtZero) {
                        scheduleSingleAlarm(
                            alarmManager,
                            baseId + 2,
                            now + realRemMs,
                            "✅ CÒN 0s — $builder ĐÃ XONG!",
                            "$name đã nâng cấp xong 100%. Vào giao việc mới ngay!",
                            vibrateOnly || !soundEnabled
                        )
                    }
                }

                // Cập nhật Widget ngoài màn hình chính
                val widgetIntent = Intent(context, CoCWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    val ids = AppWidgetManager.getInstance(context)
                        .getAppWidgetIds(ComponentName(context, CoCWidgetProvider::class.java))
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(widgetIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun scheduleSingleAlarm(
            alarmManager: AlarmManager,
            reqCode: Int,
            triggerAtMillis: Long,
            title: String,
            body: String,
            silentMode: Boolean
        ) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("REQ_CODE", reqCode)
                putExtra("TITLE", title)
                putExtra("BODY", body)
                putExtra("SILENT_MODE", silentMode)
            }
            val pi = PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } catch (se: SecurityException) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        }
    }
}
