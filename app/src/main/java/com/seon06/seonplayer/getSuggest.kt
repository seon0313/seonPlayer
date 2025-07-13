package com.seon06.seonplayer

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection


fun getSuggest(query: String): List<String> {
    val encodedQuery = URLEncoder.encode(query, "UTF-8")
    //val urlString = "http://suggestqueries.google.com/complete/search?client=firefox&q=$encodedQuery" // Google
    val urlString = "http://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=$encodedQuery" // Youtube
    return try {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val inputStream = connection.inputStream
        val text = inputStream.bufferedReader().use { it.readText() }
        val json = JSONArray(text)
        val suggestionsArray = json.getJSONArray(1)

        val suggestionsList = mutableListOf<String>()
        for (i in 0 until suggestionsArray.length()) {
            suggestionsList.add(suggestionsArray.getString(i))
        }

        suggestionsList
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}