package com.evmcstudios.joblerio.data

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object Analytics {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init() {
        firebaseAnalytics = Firebase.analytics
    }

    fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics?.logEvent(name, params)
    }

    fun trackScreenView(screenName: String) {
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        })
    }

    fun trackJobClick(jobTitle: String, company: String) {
        logEvent("job_click", Bundle().apply {
            putString("job_title", jobTitle)
            putString("company", company)
        })
    }

    fun trackJobSave(jobTitle: String, company: String) {
        logEvent("job_save", Bundle().apply {
            putString("job_title", jobTitle)
            putString("company", company)
        })
    }

    fun trackJobUnsave(jobTitle: String, company: String) {
        logEvent("job_unsave", Bundle().apply {
            putString("job_title", jobTitle)
            putString("company", company)
        })
    }

    fun trackSearch(query: String, location: String) {
        logEvent(FirebaseAnalytics.Event.SEARCH, Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
            putString("location", location)
        })
    }

    fun trackLogin(method: String) {
        logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    fun trackLogout() {
        logEvent("logout")
    }

    fun trackSkipLogin() {
        logEvent("skip_login")
    }
}
