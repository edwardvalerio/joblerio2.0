package com.evmcstudios.joblerio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.evmcstudios.joblerio.data.AlertManager
import com.evmcstudios.joblerio.data.JobsApi
import com.evmcstudios.joblerio.data.JobSearchResult
import java.util.concurrent.TimeUnit

object NotificationHelper {

    private const val CHANNEL_ID = "job_alerts"
    private const val CHANNEL_NAME = "Job Alerts"

    fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for job alerts"
                }
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "createNotificationChannel failed: ${e.message}")
        }
    }

    fun showJobAlert(context: Context, title: String, message: String, notificationId: Int) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun schedulePeriodicCheck(context: Context) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<JobAlertWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "job_alert_check",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            Log.e("NotificationHelper", "schedulePeriodicCheck failed: ${e.message}")
        }
    }

    fun cancelPeriodicCheck(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork("job_alert_check")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "cancelPeriodicCheck failed: ${e.message}")
        }
    }

    fun scheduleReEngagementCheck(context: Context) {
        try {
            val workRequest = PeriodicWorkRequestBuilder<ReEngagementWorker>(
                30, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "reengagement_check",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            Log.e("NotificationHelper", "scheduleReEngagementCheck failed: ${e.message}")
        }
    }
}

class JobAlertWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val activeAlerts = AlertManager.getActiveAlerts(context)

        for (alert in activeAlerts) {
            try {
                val result = JobsApi.searchJobs(
                    query = alert.query,
                    location = alert.location,
                    limit = 5
                )

                result.onSuccess { searchResult ->
                    if (searchResult.jobs.isNotEmpty()) {
                        val shouldNotify = when (alert.frequency) {
                            "daily" -> {
                                val dayMs = 24 * 60 * 60 * 1000L
                                System.currentTimeMillis() - alert.lastNotified > dayMs
                            }
                            "weekly" -> {
                                val weekMs = 7 * 24 * 60 * 60 * 1000L
                                System.currentTimeMillis() - alert.lastNotified > weekMs
                            }
                            else -> true
                        }

                        if (shouldNotify) {
                            NotificationHelper.showJobAlert(
                                context = context,
                                title = "New Jobs: ${alert.query}",
                                message = "${searchResult.jobs.size} new jobs found for \"${alert.query}\" in ${alert.location}",
                                notificationId = alert.id.hashCode()
                            )
                            AlertManager.updateLastNotified(context, alert.id)
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fail for individual alerts
            }
        }

        return Result.success()
    }
}
