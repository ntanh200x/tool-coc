package com.theanh.cocbuilderalert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reqCode = intent.getIntExtra("REQ_CODE", 1001)
        val title = intent.getStringExtra("TITLE") ?: "⚒️ Thợ xây sắp xong!"
        val body = intent.getStringExtra("BODY") ?: "Vào Clash of Clans kiểm tra ngay!"
        val silentMode = intent.getBooleanExtra("SILENT_MODE", false)

        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 150, 400, 150, 600), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 400, 150, 400, 150, 600), -1)
        }

        val channelId = if (silentMode) "coc_alert_vibrate_only" else "coc_alert_sound"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                if (silentMode) "Cảnh báo Chỉ Rung" else "Cảnh báo Chuông + Rung",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                if (silentMode) setSound(null, null)
            }
            nm.createNotificationChannel(channel)
        }

        val gameIntent = Intent(Intent.ACTION_VIEW, Uri.parse("clashofclans://"))
        val gamePendingIntent = PendingIntent.getActivity(
            context,
            reqCode,
            gameIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(gamePendingIntent)
            .setAutoCancel(true)
            .setSilent(silentMode)
            .build()

        nm.notify(reqCode, notif)
    }
}
