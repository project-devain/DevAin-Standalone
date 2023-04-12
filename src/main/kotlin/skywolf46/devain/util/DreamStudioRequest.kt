package skywolf46.devain.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import skywolf46.devain.data.dreamstudio.DreamStudioError
import skywolf46.devain.data.dreamstudio.DreamStudioRequestData
import skywolf46.devain.data.dreamstudio.DreamStudioResponse

const val DREAM_STUDIO_ENDPOINT = "https://api.stability.ai/v1/generation/{engine_id}/text-to-image"

object DreamStudioRequest {
    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 0
        }
    }

    private val parser = JSONParser()

    suspend fun requestGenerateImage(
        apiKey: String,
        engineId: String,
        request: DreamStudioRequestData
    ): Either<String, DreamStudioResponse> {
        val result = client.post(DREAM_STUDIO_ENDPOINT.replace("{engine_id}", engineId)) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $apiKey")
            }
            setBody(JSONObject().apply {
                this["cfg_scale"] = request.cfgScale
                this["clip_guidance_prese"] = request.clip_guidance_preset
                this["height"] = request.height
                this["width"] = request.width
                this["samples"] = 1
                this["steps"] = request.steps
                this["text_prompts"] = JSONArray().apply {
                    for ((prompt, weight) in request.prompt) {
                        add(JSONObject().apply {
                            this["text"] = prompt
                            this["weight"] = weight
                        })
                    }
                }
            }.toJSONString())
        }

        val json = parser.parse(result.bodyAsText())
        if (result.status.value != 200) {
            val error = DreamStudioError(json as JSONObject)
            return "${error.name} : ${error.message}".left()
        }
        return DreamStudioResponse(json as JSONObject).right()
    }
}