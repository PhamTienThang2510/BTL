package com.example.btl.Repository

import android.util.Log
import com.example.btl.Api.RetrofitClient
import com.example.btl.Model.LocationOption
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class LocationRepository {

    companion object {
        private const val TAG = "LocationRepository"
    }

    private val seedProvinces = listOf(
        LocationOption(1, "Ha Noi"),
        LocationOption(2, "TP Ho Chi Minh"),
        LocationOption(3, "Da Nang")
    )

    private val seedDistricts = listOf(
        LocationOption(11, "Ba Dinh", 1),
        LocationOption(12, "Dong Da", 1),
        LocationOption(21, "Quan 1", 2),
        LocationOption(22, "Quan 3", 2),
        LocationOption(31, "Hai Chau", 3),
        LocationOption(32, "Thanh Khe", 3)
    )

    private val seedWards = listOf(
        LocationOption(111, "Phuc Xa", 11),
        LocationOption(112, "Truc Bach", 11),
        LocationOption(121, "Cat Linh", 12),
        LocationOption(122, "Van Mieu", 12),
        LocationOption(211, "Ben Nghe", 21),
        LocationOption(212, "Ben Thanh", 21),
        LocationOption(221, "Vo Thi Sau", 22),
        LocationOption(311, "Hai Chau I", 31),
        LocationOption(312, "Hai Chau II", 31),
        LocationOption(321, "Thac Gian", 32)
    )

    suspend fun getProvinces(token: String): List<LocationOption> {
        val remote = fetchRemote(
            candidates = listOf(
                "provinces",
                "locations/provinces",
                "addresses/provinces",
                "addresses/locations/provinces"
            ),
            token = token
        )
        if (remote.isEmpty()) {
            Log.w(TAG, "getProvinces: fallback to seed data")
        }
        return remote.ifEmpty { seedProvinces }
    }

    suspend fun getDistricts(provinceId: Int, token: String): List<LocationOption> {
        val remoteDirect = fetchRemote(
            candidates = listOf(
                "districts?province_id=$provinceId",
                "districts?provinceId=$provinceId",
                "locations/districts?province_id=$provinceId",
                "locations/districts?provinceId=$provinceId",
                "addresses/districts?province_id=$provinceId",
                "addresses/districts?provinceId=$provinceId"
            ),
            token = token
        )

        if (remoteDirect.isNotEmpty()) return remoteDirect

        // Fallback: try unfiltered endpoint and filter by province id.
        val remoteAll = fetchRemote(
            candidates = listOf("districts", "locations/districts", "addresses/districts"),
            token = token
        )
        val filtered = remoteAll.filter { it.parentId == provinceId }
        if (filtered.isNotEmpty()) return filtered

        Log.w(TAG, "getDistricts: fallback to seed data for provinceId=$provinceId")
        return seedDistricts.filter { it.parentId == provinceId }
    }

    suspend fun getWards(districtId: Int, token: String): List<LocationOption> {
        val remoteDirect = fetchRemote(
            candidates = listOf(
                "wards?district_id=$districtId",
                "wards?districtId=$districtId",
                "locations/wards?district_id=$districtId",
                "locations/wards?districtId=$districtId",
                "addresses/wards?district_id=$districtId",
                "addresses/wards?districtId=$districtId"
            ),
            token = token
        )

        if (remoteDirect.isNotEmpty()) return remoteDirect

        // Fallback: try unfiltered endpoint and filter by district id.
        val remoteAll = fetchRemote(
            candidates = listOf("wards", "locations/wards", "addresses/wards"),
            token = token
        )
        val filtered = remoteAll.filter { it.parentId == districtId }
        if (filtered.isNotEmpty()) return filtered

        Log.w(TAG, "getWards: fallback to seed data for districtId=$districtId")
        return seedWards.filter { it.parentId == districtId }
    }

    private suspend fun fetchRemote(candidates: List<String>, token: String): List<LocationOption> {
        val tokenHeader = token.takeIf { it.isNotBlank() }
        candidates.forEach { path ->
            try {
                val response = RetrofitClient.locationApi.getByUrl(path, tokenHeader)
                if (!response.isSuccessful || response.body() == null) return@forEach

                val parsed = parseToOptions(response.body()!!)
                if (parsed.isNotEmpty()) {
                    Log.d(TAG, "fetchRemote: Loaded ${parsed.size} options from $path")
                    return parsed
                }

                Log.d(TAG, "fetchRemote: Empty parse from $path")
            } catch (e: Exception) {
                Log.d(TAG, "fetchRemote: Skip $path - ${e.message}")
            }
        }

        return emptyList()
    }

    private fun parseToOptions(json: JsonElement): List<LocationOption> {
        val array = toArray(json) ?: return emptyList()
        return array.mapNotNull { item ->
            val obj = item.asJsonObjectOrNull() ?: return@mapNotNull null
            val id = extractInt(
                obj,
                listOf("id", "province_id", "district_id", "ward_id", "provinceId", "districtId", "wardId", "code")
            )
                ?: return@mapNotNull null
            val name = extractString(
                obj,
                listOf("name", "province_name", "district_name", "ward_name", "full_name", "provinceName", "districtName", "wardName")
            )
                ?: return@mapNotNull null
            val parentId = extractInt(
                obj,
                listOf("parent_id", "parentId", "province_id", "district_id", "provinceId", "districtId")
            )
            LocationOption(id = id, name = name, parentId = parentId)
        }
    }

    private fun toArray(json: JsonElement): JsonArray? {
        if (json.isJsonArray) return json.asJsonArray
        if (!json.isJsonObject) return null

        val obj = json.asJsonObject
        val keys = listOf("data", "items", "results", "rows", "districts", "wards", "provinces")
        keys.forEach { key ->
            val element = obj.get(key)
            if (element != null && element.isJsonArray) return element.asJsonArray

            if (element != null && element.isJsonObject) {
                val nested = toArray(element)
                if (nested != null) return nested
            }
        }

        return null
    }

    private fun extractInt(obj: JsonObject, keys: List<String>): Int? {
        keys.forEach { key ->
            val value = obj.get(key) ?: return@forEach
            if (value.isJsonPrimitive) {
                val primitive = value.asJsonPrimitive
                if (primitive.isNumber) return primitive.asInt
                if (primitive.isString) return primitive.asString.toIntOrNull()
            }
        }
        return null
    }

    private fun extractString(obj: JsonObject, keys: List<String>): String? {
        keys.forEach { key ->
            val value = obj.get(key) ?: return@forEach
            if (value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                val text = value.asString.trim()
                if (text.isNotEmpty()) return text
            }
        }
        return null
    }

    private fun JsonElement.asJsonObjectOrNull(): JsonObject? {
        return if (isJsonObject) asJsonObject else null
    }
}

