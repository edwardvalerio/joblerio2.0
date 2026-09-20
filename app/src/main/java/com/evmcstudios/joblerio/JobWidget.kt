package com.evmcstudios.joblerio

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.evmcstudios.joblerio.data.JobsApi
import com.evmcstudios.joblerio.data.UserPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JobWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, JobWidget::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.evmcstudios.joblerio.ACTION_REFRESH_WIDGET"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_job_list)

            val pendingIntent = Intent(context, MainActivity::class.java).let { intent ->
                android.app.PendingIntent.getActivity(
                    context, 0, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
            }
            views.setOnClickPendingIntent(R.widget_refresh, pendingIntent)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val searchQuery = UserPrefs.getUserName(context).ifBlank { "jobs" }
                    val result = JobsApi.searchJobs(
                        query = searchQuery,
                        location = "95054",
                        limit = 5
                    )

                    result.onSuccess { searchResult ->
                        val contentView = RemoteViews(context.packageName, R.layout.widget_job_list)

                        contentView.setTextViewText(R.widget_loading, "")

                        contentView.removeAllViews(R.widget_content)

                        if (searchResult.jobs.isEmpty()) {
                            val emptyView = RemoteViews(context.packageName, R.layout.widget_job_item)
                            emptyView.setTextViewText(R.widget_job_title, "No jobs found")
                            emptyView.setTextViewText(R.widget_job_company, "Tap refresh to try again")
                            contentView.addView(R.widget_content, emptyView)
                        } else {
                            searchResult.jobs.take(5).forEach { job ->
                                val item_view = RemoteViews(context.packageName, R.layout.widget_job_item)
                                item_view.setTextViewText(R.widget_job_title, job.title)
                                item_view.setTextViewText(R.widget_job_company, "${job.company} - ${job.location}")

                                val jobIntent = Intent(context, MainActivity::class.java).apply {
                                    action = "OPEN_JOB"
                                    putExtra("job_url", job.url)
                                    putExtra("job_title", job.title)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                val jobPendingIntent = android.app.PendingIntent.getActivity(
                                    context, job.title.hashCode(), jobIntent,
                                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                )
                                item_view.setOnClickPendingIntent(R.widget_job_title, jobPendingIntent)

                                contentView.addView(R.widget_content, item_view)
                            }
                        }

                        appWidgetManager.updateAppWidget(appWidgetId, contentView)
                    }.onFailure {
                        val errorView = RemoteViews(context.packageName, R.layout.widget_job_item)
                        errorView.setTextViewText(R.widget_job_title, "Tap to refresh")
                        errorView.setTextViewText(R.widget_job_company, "")

                        val retryIntent = Intent(context, JobWidget::class.java).apply {
                            action = ACTION_REFRESH
                        }
                        val retryPendingIntent = android.app.PendingIntent.getBroadcast(
                            context, 0, retryIntent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        errorView.setOnClickPendingIntent(R.widget_job_title, retryPendingIntent)

                        val contentView = RemoteViews(context.packageName, R.layout.widget_job_list)
                        contentView.setTextViewText(R.widget_loading, "")
                        contentView.addView(R.widget_content, errorView)
                        appWidgetManager.updateAppWidget(appWidgetId, contentView)
                    }
                } catch (e: Exception) {
                    val errorView = RemoteViews(context.packageName, R.layout.widget_job_item)
                    errorView.setTextViewText(R.widget_job_title, "Tap to refresh")
                    errorView.setTextViewText(R.widget_job_company, "")

                    val contentView = RemoteViews(context.packageName, R.layout.widget_job_list)
                    contentView.setTextViewText(R.widget_loading, "")
                    contentView.addView(R.widget_content, errorView)
                    appWidgetManager.updateAppWidget(appWidgetId, contentView)
                }
            }
        }
    }
}
