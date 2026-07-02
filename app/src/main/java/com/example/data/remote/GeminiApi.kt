package com.example.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class ImageGenerationRequest(
    val instances: List<PromptInstance>,
    val parameters: ImageParameters
)

@JsonClass(generateAdapter = true)
data class PromptInstance(val prompt: String)

@JsonClass(generateAdapter = true)
data class ImageParameters(
    val sampleCount: Int = 1,
    val aspect_ratio: String = "9:16"
)

@JsonClass(generateAdapter = true)
data class ImageGenerationResponse(
    val predictions: List<Prediction>
)

@JsonClass(generateAdapter = true)
data class Prediction(
    val bytesBase64Encoded: String
)

interface GeminiApi {
    // Note: Use actual Imagen API endpoint
    @POST("v1beta/models/imagegeneration:predict")
    suspend fun generateImage(
        @Header("Authorization") authHeader: String,
        @Body request: ImageGenerationRequest
    ): ImageGenerationResponse
}
