package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.stream.JsonReader
import java.io.StringReader

object SavedJobsManager {

    private const val TAG = "SavedJobsManager"
    private const val PREFS_NAME = "saved_jobs"
    private const val KEY_SAVED_JOBS = "saved_jobs_list"

    private val gson = JobsGson.instance

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveJob(context: Context, job: Job) {
        Log.d(TAG, "saveJob called: ${job.title} at ${job.company} (id=${job.id})")
        val jobs = getSavedJobs(context).toMutableList()
        Log.d(TAG, "Current saved count: ${jobs.size}")
        if (jobs.none { it.id == job.id }) {
            jobs.add(0, job)
            val json = gson.toJson(jobs)
            getPrefs(context).edit().putString(KEY_SAVED_JOBS, json).apply()
            Log.d(TAG, "Job saved. New count: ${jobs.size}")
        } else {
            Log.d(TAG, "Job already saved, skipping")
        }
    }

    fun removeJob(context: Context, job: Job) {
        Log.d(TAG, "removeJob called: ${job.title} (id=${job.id})")
        val jobs = getSavedJobs(context).toMutableList()
        jobs.removeAll { it.id == job.id }
        val json = gson.toJson(jobs)
        getPrefs(context).edit().putString(KEY_SAVED_JOBS, json).apply()
        Log.d(TAG, "Job removed. New count: ${jobs.size}")
    }

    fun isJobSaved(context: Context, jobId: String): Boolean {
        val result = getSavedJobs(context).any { it.id == jobId }
        Log.d(TAG, "isJobSaved($jobId) = $result")
        return result
    }

    fun getSavedJobs(context: Context): List<Job> {
        val json = getPrefs(context).getString(KEY_SAVED_JOBS, null) ?: return emptyList()
        return try {
            val adapter = JobAdapter()
            val reader = JsonReader(StringReader(json))
            val jobs = mutableListOf<Job>()
            reader.beginArray()
            while (reader.hasNext()) {
                jobs.add(adapter.read(reader))
            }
            reader.endArray()
            Log.d(TAG, "getSavedJobs: loaded ${jobs.size} jobs")
            jobs
        } catch (e: Exception) {
            Log.e(TAG, "getSavedJobs error: ${e.message}")
            emptyList()
        }
    }
}
