package com.evmcstudios.joblerio.data

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

data class Job(
    val title: String = "",
    val company: String = "",
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val zip: String = "",
    val date: String = "",
    val url: String = "",
    val snippet: String = ""
) {
    val id: String
        get() = "${title}_${company}_${city}".lowercase().replace("\\s+".toRegex(), "")

    val location: String
        get() = when {
            city.isNotBlank() && state.isNotBlank() -> "$city, $state"
            city.isNotBlank() -> city
            state.isNotBlank() -> state
            else -> "Unknown"
        }

    val companyInitial: String
        get() = company.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

class JobAdapter : TypeAdapter<Job>() {
    override fun write(out: JsonWriter, value: Job) {
        out.beginObject()
        out.name("title").value(value.title)
        out.name("company").value(value.company)
        out.name("city").value(value.city)
        out.name("state").value(value.state)
        out.name("country").value(value.country)
        out.name("zip").value(value.zip)
        out.name("date").value(value.date)
        out.name("url").value(value.url)
        out.name("snippet").value(value.snippet)
        out.endObject()
    }
    override fun read(input: JsonReader): Job {
        var title = ""
        var company = ""
        var city = ""
        var state = ""
        var country = ""
        var zip = ""
        var date = ""
        var url = ""
        var snippet = ""

        input.beginObject()
        while (input.hasNext()) {
            when (input.nextName()) {
                "title", "jobtitle" -> title = input.readSafe()
                "company" -> company = input.readSafe()
                "city" -> city = input.readSafe()
                "state" -> state = input.readSafe()
                "country" -> country = input.readSafe()
                "zip" -> zip = input.readSafe()
                "date" -> date = input.readSafe()
                "url" -> url = input.readSafe()
                "snippet" -> snippet = input.readSafe()
                else -> input.skipValue()
            }
        }
        input.endObject()

        return Job(title, company, city, state, country, zip, date, url, snippet)
    }

    private fun JsonReader.readSafe(): String {
        return if (peek() == JsonToken.NULL) {
            nextNull()
            ""
        } else {
            nextString()
        }
    }
}

object JobsGson {
    val instance = GsonBuilder()
        .registerTypeAdapter(Job::class.java, JobAdapter())
        .create()
}
