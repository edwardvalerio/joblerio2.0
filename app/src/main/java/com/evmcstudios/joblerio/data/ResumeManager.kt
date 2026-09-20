package com.evmcstudios.joblerio.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ResumeManager {

    private const val PREFS_NAME = "resumes"
    private const val KEY_RESUMES = "resumes_list"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveResume(context: Context, resume: Resume) {
        val resumes = getResumes(context).toMutableList()
        val index = resumes.indexOfFirst { it.id == resume.id }
        if (index != -1) {
            resumes[index] = resume.copy(updatedAt = System.currentTimeMillis())
        } else {
            resumes.add(0, resume)
        }
        val json = Gson().toJson(resumes)
        getPrefs(context).edit().putString(KEY_RESUMES, json).apply()
    }

    fun deleteResume(context: Context, resumeId: String) {
        val resumes = getResumes(context).toMutableList()
        resumes.removeAll { it.id == resumeId }
        val json = Gson().toJson(resumes)
        getPrefs(context).edit().putString(KEY_RESUMES, json).apply()
    }

    fun getResumes(context: Context): List<Resume> {
        val json = getPrefs(context).getString(KEY_RESUMES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Resume>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getResume(context: Context, resumeId: String): Resume? {
        return getResumes(context).find { it.id == resumeId }
    }
}
