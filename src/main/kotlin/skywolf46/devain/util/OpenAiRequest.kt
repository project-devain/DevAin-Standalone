package skywolf46.devain.util

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import skywolf46.devain.data.parsed.dalle.DallEResult
import skywolf46.devain.data.parsed.edit.ParsedEditResult
import skywolf46.devain.data.parsed.gpt.GPTRequest
import skywolf46.devain.data.parsed.gpt.ParsedGPTResult
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

    suspend fun requestGpt(
        apiKey: String,
        request: GPTRequest
    ): Either<String, ParsedGPTResult> {
        val response = client.post(GPT_ENDPOINT) {
            val session = ChattingSession(-1L).addDialog(ChattingSession.DialogType.USER, request.contents)
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "Bearer $apiKey")
            }
            setBody(JSONObject().apply {
                this["model"] = request.model
                this["messages"] = session.toJson()
                if (request.temperature != -1.0) {
                    this["temperature"] = request.temperature
                }
                if (request.top_p != -1.0) {
                    this["top_p"] = request.top_p
                }
                if (request.maxToken != -1) {
                    this["max_tokens"] = request.maxToken
                }
            }.toJSONString())
        }
        if (response.status.value != 200) {
            println("Error: ${response.status}\n${response.bodyAsText()}")
            return "서버에서 정상적이지 않은 결과 값을 반환하였습니다. (${response.status.toString().trim()})".left()
        }
        val result = response.bodyAsText().let { contents ->
            Either.catch {
                parser.parse(contents) as JSONObject
            }.getOrElse {
                println(contents)
                return "JSON Parse 도중 문제가 발생하였습니다.".left()
            }
        }
        if (result.containsKey("error")) {
            return (result["error"] as JSONObject)["message"].toString().left()
        }
        return ParsedGPTResult(result).right()
    }

    suspend fun requestEdit(
        apiKey: String,
        model: String,
        input: String,
        instruction: String
    ): Either<String, ParsedEditResult> {
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