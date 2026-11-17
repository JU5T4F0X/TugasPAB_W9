package com.example.myapplication.data.remote

import com.example.myapplication.data.model.ApiResult
import com.example.myapplication.data.model.Mahasiswa
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MahasiswaApiClient(
    var baseUrl: String = DEFAULT_BASE_URL
) {
    companion object {
        const val DEFAULT_BASE_URL = "http://127.0.0.1:8000/api/mahasiswa.php"
        const val EMULATOR_URL = "http://10.0.2.2:8000/api/mahasiswa.php"
        const val LOCALHOST_URL = "http://127.0.0.1:8000/api/mahasiswa.php"
    }

    suspend fun getAll(search: String? = null): ApiResult<List<Mahasiswa>> = withContext(Dispatchers.IO) {
        try {
            val urlString = if (!search.isNullOrBlank()) {
                val encoded = URLEncoder.encode(search.trim(), "UTF-8")
                "$baseUrl?search=$encoded"
            } else {
                baseUrl
            }

            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(connection)

            if (statusCode in 200..299) {
                val json = JSONObject(responseText)
                val dataArray = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<Mahasiswa>()
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    list.add(Mahasiswa.fromJson(item))
                }
                ApiResult.Success(list)
            } else {
                val errorMsg = parseErrorMessage(responseText, "Gagal mengambil data ($statusCode)")
                ApiResult.Error(errorMsg, statusCode)
            }
        } catch (e: Exception) {
            ApiResult.Error(formatNetworkException(e))
        }
    }

    suspend fun create(mahasiswa: Mahasiswa): ApiResult<Mahasiswa> = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(baseUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(mahasiswa.toJsonObject().toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(connection)

            if (statusCode in 200..299) {
                val json = JSONObject(responseText)
                val dataObj = json.optJSONObject("data") ?: JSONObject()
                val msg = if (json.has("message")) json.optString("message") else null
                ApiResult.Success(Mahasiswa.fromJson(dataObj), msg)
            } else {
                val errorMsg = parseErrorMessage(responseText, "Gagal menambah data ($statusCode)")
                ApiResult.Error(errorMsg, statusCode)
            }
        } catch (e: Exception) {
            ApiResult.Error(formatNetworkException(e))
        }
    }

    suspend fun update(mahasiswa: Mahasiswa): ApiResult<Mahasiswa> = withContext(Dispatchers.IO) {
        try {
            val urlWithId = "$baseUrl?id=${mahasiswa.id}"
            val connection = (URL(urlWithId).openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(mahasiswa.toJsonObject().toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(connection)

            if (statusCode in 200..299) {
                val json = JSONObject(responseText)
                val dataObj = json.optJSONObject("data") ?: JSONObject()
                val msg = if (json.has("message")) json.optString("message") else null
                ApiResult.Success(Mahasiswa.fromJson(dataObj), msg)
            } else {
                val errorMsg = parseErrorMessage(responseText, "Gagal memperbarui data ($statusCode)")
                ApiResult.Error(errorMsg, statusCode)
            }
        } catch (e: Exception) {
            ApiResult.Error(formatNetworkException(e))
        }
    }

    suspend fun delete(id: Int): ApiResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val urlWithId = "$baseUrl?id=$id"
            val connection = (URL(urlWithId).openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("Accept", "application/json")
            }

            val statusCode = connection.responseCode
            val responseText = readResponse(connection)

            if (statusCode in 200..299) {
                ApiResult.Success(Unit)
            } else {
                val errorMsg = parseErrorMessage(responseText, "Gagal menghapus data ($statusCode)")
                ApiResult.Error(errorMsg, statusCode)
            }
        } catch (e: Exception) {
            ApiResult.Error(formatNetworkException(e))
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val inputStream = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        return BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
            reader.readText()
        }
    }

    private fun parseErrorMessage(responseText: String, defaultMsg: String): String {
        return try {
            val json = JSONObject(responseText)
            if (json.has("errors")) {
                val errorsObj = json.getJSONObject("errors")
                val firstKey = errorsObj.keys().asSequence().firstOrNull()
                if (firstKey != null) {
                    return errorsObj.getString(firstKey)
                }
            }
            json.optString("message", defaultMsg)
        } catch (e: Exception) {
            defaultMsg
        }
    }

    private fun formatNetworkException(e: Exception): String {
        val msg = e.localizedMessage ?: e.message ?: "Kesalahan jaringan"
        return when {
            msg.contains("ECONNREFUSED", ignoreCase = true) ->
                "Koneksi ditolak (Connection Refused). Pastikan server backend PHP sedang berjalan."
            msg.contains("timeout", ignoreCase = true) ->
                "Waktu koneksi habis. Pastikan IP server dapat dijangkau."
            msg.contains("failed to connect", ignoreCase = true) ->
                "Tidak dapat terhubung ke server ($baseUrl). Periksa pengaturan IP server."
            else -> "Gagal terhubung: $msg"
        }
    }
}
