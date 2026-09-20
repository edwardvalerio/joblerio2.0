package com.evmcstudios.joblerio

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.evmcstudios.joblerio.data.JobsApi
import com.evmcstudios.joblerio.data.FilterState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JobWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(android.content.ComponentName(context, JobWidget::class.java))
            for (id in ids) updateWidget(context, mgr, id)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.evmcstudios.joblerio.ACTION_REFRESH_WIDGET"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_job_list)

            // Setup Refresh button to trigger ACTION_REFRESH broadcast
            val refreshIntent = Intent(context, JobWidget::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

            // Show initial loading state immediately
            views.setTextViewText(R.id.widget_loading, "Loading jobs...")
            views.setViewVisibility(R.id.widget_loading, View.VISIBLE)
            views.removeAllViews(R.id.widget_content)
            appWidgetManager.updateAppWidget(widgetId, views)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val homePrefs = context.getSharedPreferences("home_state", Context.MODE_PRIVATE)
                    val query = homePrefs.getString("search_query", "")?.ifBlank { "jobs" } ?: "jobs"
                    val location = homePrefs.getString("location_query", "")?.ifBlank { "95054" } ?: "95054"

                    val result = JobsApi.searchJobs(query = query, location = location, limit = 5)
                    result.onSuccess { searchResult ->
                        val updateViews = RemoteViews(context.packageName, R.layout.widget_job_list)
                        updateViews.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)
                        
                        updateViews.setViewVisibility(R.id.widget_loading, View.GONE)
                        updateViews.removeAllViews(R.id.widget_content)

                        if (searchResult.jobs.isEmpty()) {
                            val ev = RemoteViews(context.packageName, R.layout.widget_job_item)
                            ev.setTextViewText(R.id.job_title, "No jobs found")
                            ev.setTextViewText(R.id.job_company, "Tap refresh to try again")
                            updateViews.addView(R.id.widget_content, ev)
                        } else {
                            searchResult.jobs.take(5).forEach { job ->
                                val iv = RemoteViews(context.packageName, R.layout.widget_job_item)
                                iv.setTextViewText(R.id.job_title, job.title)
                                iv.setTextViewText(R.id.job_company, "${job.company} - ${job.location}")
                                val ji = Intent(context, MainActivity::class.java).apply {
                                    action = "OPEN_JOB"
                                    putExtra("job_url", job.url)
                                    putExtra("job_title", job.title)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                val pi = PendingIntent.getActivity(context, job.url.hashCode(), ji,
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                iv.setOnClickPendingIntent(R.id.job_title, pi)
                                iv.setOnClickPendingIntent(R.id.job_company, pi)
                                updateViews.addView(R.id.widget_content, iv)
                            }
                        }
                        appWidgetManager.updateAppWidget(widgetId, updateViews)
                    }.onFailure {
                        val nv = RemoteViews(context.packageName, R.layout.widget_job_list)
                        nv.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)
                        nv.setViewVisibility(R.id.widget_loading, View.GONE)
                        
                        val ev = RemoteViews(context.packageName, R.layout.widget_job_item)
                        ev.setTextViewText(R.id.job_title, "Failed to load jobs")
                        ev.setTextViewText(R.id.job_company, "Tap here to retry")
                        ev.setOnClickPendingIntent(R.id.job_title, refreshPendingIntent)
                        nv.addView(R.id.widget_content, ev)
                        
                        appWidgetManager.updateAppWidget(widgetId, nv)
                    }
                } catch (e: Exception) {
                    Log.e("JobWidget", "Error: ${e.message}")
                }
            }
        }
    }
}
