package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_BUDGET_ID = "category_budget_alerts"
    private const val CHANNEL_BUDGET_NAME = "বাজেট সতর্কতা"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_BUDGET_ID,
                CHANNEL_BUDGET_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ক্যাটাগরি ভিত্তিক বাজেট ৮০% বা তার বেশি খরচের নোটিফিকেশন"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showCategoryBudgetWarning(
        context: Context,
        categoryName: String,
        percent: Int,
        spentPaisa: Long,
        budgetPaisa: Long
    ) {
        createNotificationChannels(context)

        // Check permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            categoryName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚠️ বাজেট সতর্কতা: $categoryName (${percent}%)"
        val message = "$categoryName খাতে আপনার নির্ধারিত বাজেটের ${percent}% ব্যবহৃত হয়েছে! খরচ: ${CurrencyFormatter.formatPaisaToTaka(spentPaisa)} / ${CurrencyFormatter.formatPaisaToTaka(budgetPaisa)}"

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGET_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(categoryName.hashCode(), notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
