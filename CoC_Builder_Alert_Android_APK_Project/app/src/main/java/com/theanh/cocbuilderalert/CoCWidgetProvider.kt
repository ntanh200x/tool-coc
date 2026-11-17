package com.theanh.cocbuilderalert

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import org.json.JSONArray

class CoCWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("coc_widget_prefs", Context.MODE_PRIVATE)
        val raw = prefs.getString("tasks_json", "[]") ?: "[]"

        var summaryBuilder = "🔨 Thợ xây: Đang rảnh"
        var summaryLab = "🔬 Phòng Lab: Đang rảnh"
        var summaryPet = "🐾 Linh thú: Đang rảnh"

        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val cat = item.optString("category", "builder")
                val remMs = item.optLong("realRemainingMs", 0L)
                if (remMs <= 0L) continue
                val totalMin = remMs / 60000L
                val h = totalMin / 60
                val m = totalMin % 60
                val name = item.optString("name", "")
                val line = "${name}: còn ${h}h ${m}p"

                if (cat == "builder" && summaryBuilder.endsWith("Đang rảnh")) {
                    summaryBuilder = "🔨 $line"
                } else if (cat == "lab" && summaryLab.endsWith("Đang rảnh")) {
                    summaryLab = "🔬 $line"
                } else if (cat == "pet" && summaryPet.endsWith("Đang rảnh")) {
                    summaryPet = "🐾 $line"
                }
            }
        } catch (e: Exception) {}

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.coc_widget)
            views.setTextViewText(R.id.tvWidgetBuilder, summaryBuilder)
            views.setTextViewText(R.id.tvWidgetLab, summaryLab)
            views.setTextViewText(R.id.tvWidgetPet, summaryPet)

            val openAppIntent = Intent(context, MainActivity::class.java)
            val pi = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pi)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
