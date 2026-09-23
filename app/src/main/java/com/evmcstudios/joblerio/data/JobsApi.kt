package com.evmcstudios.joblerio.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class JobSearchResult(
    val jobs: List<Job>,
    val totalResults: Int
)

data class LocationResult(
    val location: String,
    val countryCode: String
)

object JobsApi {

    private const val PAGE_SIZE = 10

    internal val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun getDeviceIp(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress ?: "0.0.0.0"
                    }
                }
            }
        } catch (_: Exception) {}
        return "0.0.0.0"
    }

    private fun getUserAgent(): String {
        return "Mozilla/5.0 (Linux; Android ${Build.VERSION.RELEASE}; ${Build.MODEL}) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
    }

    suspend fun detectLocation(context: Context): LocationResult? = withContext(Dispatchers.IO) {
        try {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.d("JobsApi", "No location permission")
                return@withContext null
            }

            val fusedClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
            val location = fusedClient.lastLocation.await()

            if (location != null) {
                Log.d("JobsApi", "Got location: ${location.latitude}, ${location.longitude}")
                val geocoder = Geocoder(context, java.util.Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.firstOrNull()
                    if (address != null) {
                        val city = address.locality ?: address.subAdminArea ?: ""
                        val state = address.adminArea ?: ""
                        val countryCode = address.countryCode ?: ""
                        val result = when {
                            city.isNotBlank() && state.isNotBlank() -> "$city, $state"
                            city.isNotBlank() -> city
                            state.isNotBlank() -> state
                            else -> ""
                        }
                        Log.d("JobsApi", "Geocoded location: $result, country: $countryCode")
                        return@withContext LocationResult(result, countryCode)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val address = addresses?.firstOrNull()
                    if (address != null) {
                        val city = address.locality ?: address.subAdminArea ?: ""
                        val state = address.adminArea ?: ""
                        val countryCode = address.countryCode ?: ""
                        val result = when {
                            city.isNotBlank() && state.isNotBlank() -> "$city, $state"
                            city.isNotBlank() -> city
                            state.isNotBlank() -> state
                            else -> ""
                        }
                        Log.d("JobsApi", "Geocoded location: $result, country: $countryCode")
                        return@withContext LocationResult(result, countryCode)
                    }
                }
            }
            Log.d("JobsApi", "No location obtained")
            null
        } catch (e: Exception) {
            Log.e("JobsApi", "Location detection failed: ${e.message}")
            null
        }
    }

    suspend fun searchJobs(
        query: String,
        location: String,
        filter: FilterState = FilterState(),
        start: Int = 0,
        limit: Int = PAGE_SIZE
    ): Result<JobSearchResult> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query.ifBlank { "jobs" }, "UTF-8")
            val encodedLocation = URLEncoder.encode(location.ifBlank { "95054" }, "UTF-8")

            val baseUrl = RemoteConfigManager.getJobsApiUrl()
            val apiKey = RemoteConfigManager.getJobsApiKey()

            var url = "$baseUrl" +
                "?q=$encodedQuery" +
                "&l=$encodedLocation" +
                "&r=${filter.radius}" +
                "&s=${filter.sortBy}" +
                "&start=$start" +
                "&limit=$limit" +
                "&userip=${getDeviceIp()}" +
                "&useragent=${URLEncoder.encode(getUserAgent(), "UTF-8")}"

            if (filter.categories.isNotEmpty()) {
                val catParams = JobCategories.getCategoryParam(filter.categories)
                url += "&$catParams"
            }

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .header("X-API-Key", apiKey)
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API error: ${response.code}"))
            }

            val body = response.body?.string()
                ?: return@withContext Result.failure(Exception("Empty response"))

            val jsonResponse = com.google.gson.JsonParser.parseString(body).asJsonArray
            val responseObj = jsonResponse.firstOrNull()?.asJsonObject?.getAsJsonObject("response")
                ?: return@withContext Result.failure(Exception("No results"))

            val totalResults = responseObj.get("totalresults")?.asString ?: "0"
            val resultsArray = responseObj.getAsJsonArray("results") ?: com.google.gson.JsonArray()

            val jobAdapter = JobAdapter()
            val jobs = resultsArray.mapNotNull { element ->
                try {
                    jobAdapter.read(com.google.gson.stream.JsonReader(java.io.StringReader(element.toString())))
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(
                JobSearchResult(
                    jobs = jobs,
                    totalResults = totalResults.toIntOrNull() ?: 0
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
