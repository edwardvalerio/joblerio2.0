package com.evmcstudios.joblerio.data

import android.os.Build
import kotlinx.coroutines.Dispatchers
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

object JobsApi {

    private const val BASE_URL = "https://api.l5srv.net/job_search/api/web/find_jobs.srv"
    private const val CID = "4183"
    private const val CHID = "app"
    private const val PAGE_SIZE = 10
    private const val TEST_MODE = true
    private const val TEST_URL = "https://evmcstudios.com/"

    private val client = OkHttpClient.Builder()
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

    suspend fun searchJobs(
        query: String,
        location: String,
        radius: Int = 25,
        start: Int = 0,
        limit: Int = PAGE_SIZE
    ): Result<JobSearchResult> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query.ifBlank { "jobs" }, "UTF-8")
            val encodedLocation = URLEncoder.encode(location.ifBlank { "95054" }, "UTF-8")

            val url = "$BASE_URL" +
                "?CID=$CID" +
                "&CHID=$CHID" +
                "&format=JSON2" +
                "&q=$encodedQuery" +
                "&l=$encodedLocation" +
                "&r=$radius" +
                "&s=relevance" +
                "&start=$start" +
                "&limit=$limit" +
                "&userip=${getDeviceIp()}" +
                "&useragent=${URLEncoder.encode(getUserAgent(), "UTF-8")}"

            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
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
                    val job = jobAdapter.read(com.google.gson.stream.JsonReader(java.io.StringReader(element.toString())))
                    if (TEST_MODE) job.copy(url = TEST_URL) else job
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
