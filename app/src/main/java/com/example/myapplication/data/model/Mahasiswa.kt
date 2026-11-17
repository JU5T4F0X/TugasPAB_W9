package com.example.myapplication.data.model

import org.json.JSONObject

data class Mahasiswa(
    val id: Int = 0,
    val nim: String,
    val nama: String,
    val jurusan: String,
    val email: String,
    val alamat: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            if (id > 0) put("id", id)
            put("nim", nim)
            put("nama", nama)
            put("jurusan", jurusan)
            put("email", email)
            put("alamat", alamat ?: "")
        }
    }

    companion object {
        fun fromJson(json: JSONObject): Mahasiswa {
            val alamatVal = if (json.has("alamat") && !json.isNull("alamat")) {
                json.optString("alamat").takeIf { it.isNotBlank() }
            } else null

            return Mahasiswa(
                id = json.optInt("id", 0),
                nim = json.optString("nim", ""),
                nama = json.optString("nama", ""),
                jurusan = json.optString("jurusan", ""),
                email = json.optString("email", ""),
                alamat = alamatVal,
                createdAt = if (json.has("created_at") && !json.isNull("created_at")) json.optString("created_at") else null,
                updatedAt = if (json.has("updated_at") && !json.isNull("updated_at")) json.optString("updated_at") else null
            )
        }
    }
}
