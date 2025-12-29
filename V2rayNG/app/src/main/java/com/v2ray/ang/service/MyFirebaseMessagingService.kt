package com.v2ray.ang.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.ui.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * زمانی صدا زده می‌شود که توکن جدیدی ساخته شود.
     * بهتر است این توکن را جایی ذخیره کنید تا در اجرای بعدی هندشیک به سرور ارسال شود.
     */
    override fun onNewToken(token: String) {
        Log.d(AppConfig.TAG, "Refreshed token: $token")
        // ذخیره توکن در SharedPreferences برای استفاده‌های بعدی اگر لازم شد
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("fcm_token_cache", token).apply()
    }

    /**
     * زمانی صدا زده می‌شود که پیامی از سمت فایربیس دریافت شود.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(AppConfig.TAG, "From: ${remoteMessage.from}")

        // بررسی اینکه آیا پیام حاوی نوتیفیکیشن است
        remoteMessage.notification?.let {
            Log.d(AppConfig.TAG, "Message Notification Body: ${it.body}")
            sendNotification(it.title, it.body)
        }
        
        // اگر پیام حاوی دیتا (Data Payload) باشد هم اینجا هندل می‌شود
        if (remoteMessage.data.isNotEmpty()) {
             val title = remoteMessage.data["title"]
             val body = remoteMessage.data["body"]
             if(!title.isNullOrEmpty() || !body.isNullOrEmpty()){
                 sendNotification(title, body)
             }
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name) // آیکون برنامه
            .setContentTitle(title ?: getString(R.string.app_name))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // برای اندروید Oreo و بالاتر باید کانال نوتیفیکیشن بسازیم
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "پیام‌های سیستم", // نام کانال که کاربر می‌بیند
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}