package com.example.btl.Repository

import android.content.Context
import android.util.Log
import com.example.btl.Api.MockupApi
import com.example.btl.Api.RetrofitClient
import com.example.btl.DataStore.DataStoreManager
import com.example.btl.Model.CreateMockupRenderDto
import com.example.btl.Model.MockupRenderResponseDto
import com.example.btl.Model.MockupTemplateModel
import com.example.btl.Model.Product
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Repository for Mockup operations
 * Handles API calls for uploading images and rendering mockups
 */
class MockupRepository(private val context: Context) {
    private val mockupApi: MockupApi
    private val TAG = "MockupRepository"
    private val gson = Gson()

    // Demo mode flag - set to true to use hardcoded image for testing
    companion object {
        var DEMO_MODE = false
        const val DEMO_IMAGE_URL = "https://preview.redd.it/illit-minju-v0-zh6hvlwlen8d1.jpg?width=850&format=pjpg&auto=webp&s=360ee513444fe18a89335bb79da5295896eb4e4e"
        const val PRODUCT_BUTTON_IMAGE_URL = "https://th.bing.com/th/id/OIP.m-x0zKlCZiz6A4R2X9uJHAHaLH?o=7rm=3&rs=1&pid=ImgDetMain&o=7&rm=3"
        const val TEMPLATE_PLACEHOLDER_URL = "https://via.placeholder.com/200x200?text=Template"
    }

    private data class TemplateCandidate(
        val templateId: Int,
        val isActive: Boolean,
        val smartObjects: String?
    )

    init {
        // Initialize RetrofitClient with context
        RetrofitClient.init(context)
        // Get mockupApi from RetrofitClient
        mockupApi = RetrofitClient.mockupApi
    }

    private fun generateRenderId(): Int {
        return ((System.currentTimeMillis() / 1000) % 100000).toInt()
    }

    private fun nowIsoUtc(): String {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
        format.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return format.format(java.util.Date())
    }

    /**
     * Create mockup render from a remote design image URL.
     *
     * In DEMO_MODE: Returns hardcoded image URL for testing without backend
     * In NORMAL_MODE: Calls backend render API and gets rendered image
     *
     * @param designImageUrl Remote image URL used as design input
     * @param templateId ID of the template to use for rendering
     * @param productId Optional product ID
     * @return MockupRenderResponseDto if successful, null otherwise
     */
    suspend fun uploadAndRenderMockup(
        designImageUrl: String,
        templateId: Int,
        productId: Int? = null
    ): MockupRenderResponseDto? = withContext(Dispatchers.IO) {
        // DEMO MODE: Return hardcoded image for quick testing
        if (DEMO_MODE) {
            Log.d(TAG, "🎬 DEMO MODE ENABLED - Using hardcoded image for testing")
            return@withContext MockupRenderResponseDto(
                render_id = (System.currentTimeMillis() % 10000).toInt(),
                template_id = templateId,
                status = "success",
                design_image_url = designImageUrl,
                rendered_image_url = DEMO_IMAGE_URL,
                error_message = null
            )
        }

        try {
            Log.d(TAG, "[INPUT] uploadAndRenderMockup: templateId=$templateId, productId=$productId, designImageUrl=${designImageUrl.take(120)}")

            val token = DataStoreManager.getToken(context)
            val authHeader = if (token.isNullOrBlank()) null else "Bearer $token"
            val request = CreateMockupRenderDto(
                template_id = templateId,
                design_image_url = designImageUrl,
                product_id = productId
            )

            Log.d(TAG, "[HTTP] POST mockups/render payload=${gson.toJson(request)}")
            val response = mockupApi.createRenderFixed(request)

            if (!response.isSuccessful || response.body() == null) {
                Log.w(TAG, "[ERROR] Render endpoint mockups/render failed: code=${response.code()}, message=${response.message()}, errorBody=${readErrorBody(response)}")
                return@withContext null
            }

            Log.d(TAG, "[OUTPUT] Render endpoint mockups/render body=${safeJsonPreview(response.body())}")
            return@withContext parseRenderResponse(response.body()!!, templateId)
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Exception during uploadAndRenderMockup", e)
            return@withContext null
        }
    }

    suspend fun createMockupForProduct(product: Product): MockupRenderResponseDto? = withContext(Dispatchers.IO) {
        try {
            val variantId = product.variants.firstOrNull()?.variant_id
            if (variantId == null) {
                Log.e(TAG, "No variant_id found for product ${product.product_id}")
                return@withContext null
            }

            val token = DataStoreManager.getToken(context)
            val authHeader = if (token.isNullOrBlank()) null else "Bearer $token"

            val template = fetchTemplateForVariant(variantId, authHeader)
            if (template == null) {
                Log.e(TAG, "No active template found for variant_id=$variantId")
                return@withContext null
            }

            createRenderWithTemplate(
                template = template,
                designImageUrl = PRODUCT_BUTTON_IMAGE_URL,
                productId = product.product_id,
                authHeader = authHeader
            )
        } catch (e: Exception) {
            Log.e(TAG, "Exception during createMockupForProduct", e)
            null
        }
    }

    suspend fun createMockupForVariant(
        variantId: Int,
        productId: Int?,
        designImageUrl: String = PRODUCT_BUTTON_IMAGE_URL
    ): MockupRenderResponseDto? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "[INPUT] createMockupForVariant: variantId=$variantId, productId=$productId, designImageUrl=${designImageUrl.take(120)}")
            val token = DataStoreManager.getToken(context)
            val authHeader = if (token.isNullOrBlank()) null else "Bearer $token"

            val template = fetchTemplateForVariant(variantId, authHeader)
            if (template == null) {
                Log.e(TAG, "[ERROR] No active template found for variant_id=$variantId")
                return@withContext null
            }

            Log.d(
                TAG,
                "[OUTPUT] Selected template for variant: templateId=${template.templateId}, isActive=${template.isActive}, smartObjectsLength=${template.smartObjects?.length ?: 0}"
            )

            createRenderWithTemplate(
                template = template,
                designImageUrl = designImageUrl,
                productId = productId,
                authHeader = authHeader
            )
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Exception during createMockupForVariant", e)
            null
        }
    }

    suspend fun createMockupForSelectedTemplate(
        variantId: Int,
        templateId: Int,
        productId: Int?,
        designImageUrl: String
    ): MockupRenderResponseDto? = withContext(Dispatchers.IO) {
        try {
            Log.d(
                TAG,
                "[INPUT] createMockupForSelectedTemplate: variantId=$variantId, templateId=$templateId, productId=$productId, designImageUrl=${designImageUrl.take(120)}"
            )
            val token = DataStoreManager.getToken(context)
            val authHeader = if (token.isNullOrBlank()) null else "Bearer $token"

            Log.d(TAG, "[HTTP] GET mockups/templates?variant_id=$variantId")
            val response = mockupApi.getTemplatesFixed(
                variantId = variantId
            )

            if (!response.isSuccessful || response.body() == null) {
                Log.w(
                    TAG,
                    "[ERROR] Template endpoint mockups/templates failed: code=${response.code()}, message=${response.message()}, errorBody=${readErrorBody(response)}"
                )
                return@withContext null
            }

            Log.d(TAG, "[OUTPUT] Template endpoint mockups/templates body=${safeJsonPreview(response.body())}")
            val templates = parseTemplatesResponse(response.body()!!)
            val selectedTemplate = templates.firstOrNull { it.templateId == templateId }
            if (selectedTemplate == null) {
                Log.e(TAG, "[ERROR] Selected template not found. templateId=$templateId, available=${templates.map { it.templateId }}")
                return@withContext null
            }

            createRenderWithTemplate(
                template = selectedTemplate,
                designImageUrl = designImageUrl,
                productId = productId,
                authHeader = authHeader
            )
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Exception during createMockupForSelectedTemplate", e)
            null
        }
    }

    private suspend fun createRenderWithTemplate(
        template: TemplateCandidate,
        designImageUrl: String,
        productId: Int?,
        authHeader: String?
    ): MockupRenderResponseDto? {
        val renderConfig = parseRenderConfig(template.smartObjects)
        val request = CreateMockupRenderDto(
            template_id = template.templateId,
            design_image_url = designImageUrl,
            product_id = productId,
            render_config = renderConfig
        )

        Log.d(
            TAG,
            "[INPUT] createRenderWithTemplate: templateId=${template.templateId}, productId=$productId, hasRenderConfig=${renderConfig != null}, payload=${gson.toJson(request)}"
        )

        Log.d(TAG, "[HTTP] POST mockups/render")
        val response = mockupApi.createRenderFixed(request)

        if (!response.isSuccessful || response.body() == null) {
            Log.w(TAG, "[ERROR] Render endpoint mockups/render failed: code=${response.code()}, message=${response.message()}, errorBody=${readErrorBody(response)}")
            return null
        }

        Log.d(TAG, "[OUTPUT] Render endpoint mockups/render body=${safeJsonPreview(response.body())}")

        return parseRenderResponse(response.body()!!, template.templateId)
    }

    private suspend fun fetchTemplateForVariant(
        variantId: Int,
        authHeader: String?
    ): TemplateCandidate? {
        Log.d(TAG, "[HTTP] GET mockups/templates?variant_id=$variantId")
        val response = mockupApi.getTemplatesFixed(
            variantId = variantId
        )

        if (!response.isSuccessful || response.body() == null) {
            Log.w(TAG, "[ERROR] Template endpoint mockups/templates failed: code=${response.code()}, message=${response.message()}, errorBody=${readErrorBody(response)}")
            return null
        }

        Log.d(TAG, "[OUTPUT] Template endpoint mockups/templates body=${safeJsonPreview(response.body())}")

        val templates = parseTemplatesResponse(response.body()!!)
        if (templates.isNotEmpty()) {
            val activeTemplate = templates.firstOrNull { it.isActive } ?: templates.first()
            Log.d(TAG, "[OUTPUT] Template selected: templateId=${activeTemplate.templateId}, isActive=${activeTemplate.isActive}")
            return activeTemplate
        }

        Log.w(TAG, "[ERROR] No templates parsed from endpoint=mockups/templates")
        return null
    }

    suspend fun fetchTemplates(
        variantId: Int?,
        activeOnly: Boolean = true
    ): Result<List<MockupTemplateModel>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "[HTTP] GET mockups/templates?variant_id=$variantId")
            val token = DataStoreManager.getToken(context)
            val authHeader = if (token.isNullOrBlank()) null else "Bearer $token"
            val response = mockupApi.getTemplatesFixed(
                variantId = variantId
            )

            if (!response.isSuccessful || response.body() == null) {
                Log.w(
                    TAG,
                    "[ERROR] Template endpoint mockups/templates failed: code=${response.code()}, message=${response.message()}, errorBody=${readErrorBody(response)}"
                )
                return@withContext Result.failure(Exception("Template API error ${response.code()}"))
            }

            Log.d(TAG, "[OUTPUT] Template endpoint mockups/templates body=${safeJsonPreview(response.body())}")
            val templates = parseTemplateModels(response.body()!!, activeOnly)
            Result.success(templates)
        } catch (e: Exception) {
            Log.e(TAG, "[ERROR] Exception during fetchTemplates", e)
            Result.failure(e)
        }
    }

    private fun parseRenderResponse(json: JsonElement, templateId: Int): MockupRenderResponseDto? {
        Log.d(TAG, "[OUTPUT] parseRenderResponse raw=${safeJsonPreview(json)}")
        val obj = when {
            json.isJsonObject -> {
                val raw = json.asJsonObject
                when {
                    raw.has("render_id") -> raw
                    raw.has("data") && raw.get("data").isJsonObject -> raw.getAsJsonObject("data")
                    raw.has("result") && raw.get("result").isJsonObject -> raw.getAsJsonObject("result")
                    else -> return null
                }
            }
            else -> return null
        }

        val renderId = extractInt(obj, "render_id", "renderId", "id") ?: return null
        val status = extractString(obj, "status") ?: "completed"
        val designImage = extractString(obj, "design_image_url", "designImageUrl") ?: ""
        val renderedImage = extractString(
            obj,
            "rendered_image_url",
            "renderedImageUrl",
            "mockup_image_url",
            "mockupImageUrl",
            "image_url",
            "imageUrl"
        )
        val errorMessage = extractString(obj, "error_message", "errorMessage", "message")
        val responseTemplateId = extractInt(obj, "template_id", "templateId")
        val resolvedTemplateId = responseTemplateId?.takeIf { it > 0 } ?: templateId
        if (responseTemplateId != null && responseTemplateId <= 0) {
            Log.w(TAG, "[WARN] Backend returned invalid template_id=$responseTemplateId, fallback to request templateId=$templateId")
        }

        return MockupRenderResponseDto(
            render_id = renderId,
            template_id = resolvedTemplateId,
            status = status,
            design_image_url = designImage,
            rendered_image_url = renderedImage,
            error_message = errorMessage
        )
    }

    private fun parseTemplatesResponse(json: JsonElement): List<TemplateCandidate> {
        val templateObjects = mutableListOf<JsonObject>()

        when {
            json.isJsonArray -> json.asJsonArray.forEach { if (it.isJsonObject) templateObjects.add(it.asJsonObject) }
            json.isJsonObject -> {
                val root = json.asJsonObject
                val keys = listOf("data", "templates", "result", "items")
                var extracted = false
                for (key in keys) {
                    val value = root.get(key) ?: continue
                    if (value.isJsonArray) {
                        value.asJsonArray.forEach { if (it.isJsonObject) templateObjects.add(it.asJsonObject) }
                        extracted = true
                        break
                    }
                }
                if (!extracted && root.has("template_id")) {
                    templateObjects.add(root)
                }
            }
        }

        val parsed = templateObjects.mapNotNull { obj ->
            val templateId = extractInt(obj, "template_id", "templateId", "id") ?: return@mapNotNull null
            val isActive = when {
                obj.get("is_active")?.isJsonPrimitive == true -> obj.get("is_active").asBoolean
                obj.get("isActive")?.isJsonPrimitive == true -> obj.get("isActive").asBoolean
                else -> true
            }
            val smartObjects = extractString(obj, "smart_objects", "smartObjects")
            TemplateCandidate(templateId = templateId, isActive = isActive, smartObjects = smartObjects)
        }

        Log.d(TAG, "[OUTPUT] Parsed templates count=${parsed.size}, ids=${parsed.map { it.templateId }}")
        return parsed
    }

    private fun parseTemplateModels(json: JsonElement, activeOnly: Boolean): List<MockupTemplateModel> {
        val templateObjects = mutableListOf<JsonObject>()

        when {
            json.isJsonArray -> json.asJsonArray.forEach { if (it.isJsonObject) templateObjects.add(it.asJsonObject) }
            json.isJsonObject -> {
                val root = json.asJsonObject
                val keys = listOf("data", "templates", "result", "items")
                var extracted = false
                for (key in keys) {
                    val value = root.get(key) ?: continue
                    if (value.isJsonArray) {
                        value.asJsonArray.forEach { if (it.isJsonObject) templateObjects.add(it.asJsonObject) }
                        extracted = true
                        break
                    }
                }
                if (!extracted && root.has("template_id")) {
                    templateObjects.add(root)
                }
            }
        }

        val parsed = templateObjects.mapNotNull { obj ->
            val templateId = extractInt(obj, "template_id", "templateId", "id") ?: return@mapNotNull null
            val isActive = when {
                obj.get("is_active")?.isJsonPrimitive == true -> obj.get("is_active").asBoolean
                obj.get("isActive")?.isJsonPrimitive == true -> obj.get("isActive").asBoolean
                else -> true
            }
            if (activeOnly && !isActive) return@mapNotNull null

            val name = extractString(obj, "name", "template_name", "title") ?: "Template #$templateId"
            val description = extractString(obj, "description", "desc") ?: ""
            val imageUrl = extractString(
                obj,
                "image_url",
                "thumbnail_url",
                "preview_url",
                "mockup_image_url",
                "imageUrl"
            ) ?: TEMPLATE_PLACEHOLDER_URL

            MockupTemplateModel(
                template_id = templateId,
                name = name,
                description = description,
                image_url = imageUrl
            )
        }

        Log.d(TAG, "[OUTPUT] Parsed template models count=${parsed.size}, ids=${parsed.map { it.template_id }}")
        return parsed
    }

    private fun parseRenderConfig(smartObjectsRaw: String?): Map<String, Any>? {
        if (smartObjectsRaw.isNullOrBlank()) return null

        return try {
            val element = JsonParser.parseString(smartObjectsRaw)
            when {
                element.isJsonObject -> {
                    val map = jsonObjectToNormalizedMap(element.asJsonObject)
                    @Suppress("UNCHECKED_CAST")
                    val nested = map["smart_objects"] as? List<Map<String, Any>>
                    if (!nested.isNullOrEmpty()) nested.first() else map
                }
                element.isJsonArray -> {
                    val list = element.asJsonArray.mapNotNull { item ->
                        if (!item.isJsonObject) return@mapNotNull null
                        jsonObjectToNormalizedMap(item.asJsonObject)
                    }
                    list.firstOrNull()
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.w(TAG, "[ERROR] Cannot parse smart_objects to render_config. raw=${smartObjectsRaw.take(200)}", e)
            null
        }
    }

    private fun safeJsonPreview(json: JsonElement?): String {
        if (json == null) return "null"
        val raw = json.toString()
        return if (raw.length > 600) raw.take(600) + "..." else raw
    }

    private fun readErrorBody(response: Response<*>): String {
        return try {
            val raw = response.errorBody()?.string().orEmpty()
            if (raw.isBlank()) return "<empty-error-body>"

            val parsedMessage = tryExtractBackendErrorMessage(raw)
            val merged = if (parsedMessage != null && !raw.contains(parsedMessage)) {
                "$parsedMessage | raw=$raw"
            } else {
                parsedMessage ?: raw
            }

            if (merged.length > 600) merged.take(600) + "..." else merged
        } catch (_: Exception) {
            "<errorBody-unavailable>"
        }
    }

    private fun tryExtractBackendErrorMessage(raw: String): String? {
        return try {
            val element = JsonParser.parseString(raw)
            if (!element.isJsonObject) return null
            val obj = element.asJsonObject
            extractString(obj, "error_message", "error", "message", "detail")
        } catch (_: Exception) {
            null
        }
    }

    private fun jsonObjectToNormalizedMap(obj: JsonObject): Map<String, Any> {
        return obj.entrySet().associate { (key, value) -> key to jsonElementToNormalizedValue(value) }
    }

    private fun jsonElementToNormalizedValue(element: JsonElement): Any {
        return when {
            element.isJsonObject -> jsonObjectToNormalizedMap(element.asJsonObject)
            element.isJsonArray -> element.asJsonArray.map { jsonElementToNormalizedValue(it) }
            element.isJsonNull -> ""
            else -> {
                val primitive = element.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isString -> primitive.asString
                    primitive.isNumber -> {
                        val number = primitive.asDouble
                        if (number % 1.0 == 0.0) number.toInt() else number
                    }
                    else -> primitive.toString()
                }
            }
        }
    }

    private fun extractString(obj: com.google.gson.JsonObject, vararg keys: String): String? {
        keys.forEach { key ->
            val v = obj.get(key)
            if (v != null && v.isJsonPrimitive && v.asJsonPrimitive.isString) {
                return v.asString
            }
        }
        return null
    }

    private fun extractInt(obj: com.google.gson.JsonObject, vararg keys: String): Int? {
        keys.forEach { key ->
            val v = obj.get(key)
            if (v != null && v.isJsonPrimitive) {
                val p = v.asJsonPrimitive
                if (p.isNumber) return p.asInt
                if (p.isString) return p.asString.toIntOrNull()
            }
        }
        return null
    }

}
