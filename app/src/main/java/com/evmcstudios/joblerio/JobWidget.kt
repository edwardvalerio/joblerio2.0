package com.evmcstudios.joblerio

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.evmcstudios.joblerio.data.JobsApi
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

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.widget_refresh,
                android.app.PendingIntent.getActivity(context, 0, clickIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = JobsApi.searchJobs(query = "jobs", location = "95054", limit = 5)
                    result.onSuccess { searchResult ->
                        val newViews = RemoteViews(context.packageName, R.layout.widget_job_list)
                        newViews.setOnClickPendingIntent(R.id.widget_refresh,
                            android.app.PendingIntent.getActivity(context, 0, clickIntent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE))
                        newViews.setTextViewText(R.id.widget_loading, "")
                        newViews.removeAllViews(R.id.widget_content)

                        if (searchResult.jobs.isEmpty()) {
                            val ev = RemoteViews(context.packageName, R.layout.widget_job_item)
                            ev.setTextViewText(R.id.job_title, "No jobs found")
                            ev.setTextViewText(R.id.job_company, "Tap to refresh")
                            newViews.addView(R.id.widget_content, ev)
                        } else {
                            searchResult.jobs.take(5).forEach { job ->
                                val iv = RemoteViews(context.packageName, R.layout.widget_job_item)
                                iv.setTextViewText(R.id.job_title, job.title)
                                iv.setTextViewText(R.id.job_company, "${job.company} - ${job.location}")
                                val ji = Intent(context, MainActivity::class.java).apply {
                                    putExtra("job_url", job.url)
                                    putExtra("job_title", job.title)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                                iv.setOnClickPendingIntent(R.id.job_title,
                                    android.app.PendingIntent.getActivity(context, job.title.hashCode(), ji,
                                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE))
                                newViews.addView(R.id.widget_content, iv)
                            }
                        }
                        appWidgetManager.updateAppWidget(widgetId, newViews)
                    }.onFailure {
                        val ev = RemoteViews(context.packageName, R.layout.widget_job_item)
                        ev.setTextViewText(R.id.job_title, "Tap to refresh")
                        ev.setTextViewText(R.id.job_company, "")
                        val nv = RemoteViews(context.packageName, R.layout.widget_job_list)
                        nv.setTextViewText(R.id.widget_loading, "")
                        nv.addView(R.id.widget_content, ev)
                        appWidgetManager.updateAppWidget(widgetId, nv)
                    }
                } catch (e: Exception) {
                    Log.e("JobWidget", "Error: ${e.message}")
                    val ev = RemoteViews(context.packageName, R.layout.widget_job_item)
                    ev.setTextViewText(R.id.job_title, "Tap to refresh")
                    ev.setTextViewText(R.id.job_company, "")
                    val nv = RemoteViews(context.packageName, R.layout.widget_job_list)
                    nv.setTextViewText(R.id.widget_loading, "")
                    nv.addView(R.id.widget_content, ev)
                    appWidgetManager.updateAppWidget(widgetId, nv)
                }
            }
        }
    }
}
