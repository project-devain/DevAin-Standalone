package skywolf46.devain.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import skywolf46.devain.data.dalle.DallEResult
import skywolf46.devain.data.edit.ParsedEditResult
import skywolf46.devain.data.gpt.ParsedGPTResult
import skywolf46.devain.sessions.ChattingSession

const val GPT_ENDPOINT = "https://api.openai.com/v1/chat/completions"
const val DALL_E_ENDPOINT = "https://api.openai.com/v1/images/generations"
const val EDIT_ENDPOINT = "https://api.openai.com/v1/edits"

object OpenAiRequest {
    private val client = HttpClient(CIO) {
        engine {
            requestTimeout = 0
        }
    }
    private val parser = JSONParser()

    suspend fun requestGpt(apiKey: String, model: String, message: String): Either<String, ParsedGPTResult> {
        val result = client.post(GPT_ENDPOINT) {
            val session = ChattingSession(-1L).addDialog(ChattingSession.DialogType.USER, message)
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $apiKey")
            }
            setBody(JSONObject().apply {
                this["model"] = model
                this["messages"] = session.toJson()
            }.toJSONString())
        }.bodyAsText().let {
            parser.parse(it) as JSONObject
        }
        if (result.containsKey("error")) {
            return (result["error"] as JSONObject)["message"].toString().left()
        }
        return ParsedGPTResult(result).right()
    }

    suspend fun requestEdit(apiKey: String, model: String, input: String, instruction: String): Either<String, ParsedEditResult> {
        val result = client.post(EDIT_ENDPOINT) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $apiKey")
            }
            setBody(JSONObject().apply {
                this["model"] = model
                this["input"] = input
                this["instruction"] = instruction
            }.toJSONString())
        }.bodyAsText().let {
            parser.parse(it) as JSONObject
        }
        if (result.containsKey("error")) {
            return (result["error"] as JSONObject)["message"].toString().left()
        }
        return ParsedEditResult(result).right()
    }

    suspend fun requestDallE(apiKey: String, prompt: String): Either<String, DallEResult> {
        val result = client.post(DALL_E_ENDPOINT) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $apiKey")
            }
            setBody(JSONObject().apply {
                this["prompt"] = prompt
                this["n"] = 1
                this["size"] = "1024x1024"
            }.toJSONString())
        }.bodyAsText().let {
            parser.parse(it) as JSONObject
        }
        if (result.containsKey("error")) {
            return (result["error"] as JSONObject)["message"].toString().left()
        }
        return DallEResult(result).right()
    }
}