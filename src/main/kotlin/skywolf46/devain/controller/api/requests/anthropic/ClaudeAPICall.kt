package skywolf46.devain.controller.api.requests.anthropic

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import io.ktor.client.*
import io.ktor.http.*
import org.json.simple.JSONObject
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.RESTAPICall
import skywolf46.devain.model.api.anthropic.ClaudeGenerationRequest
import skywolf46.devain.model.api.anthropic.ClaudeGenerationResponse

class ClaudeAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    RESTAPICall<ClaudeGenerationRequest, ClaudeGenerationResponse>(
        { "https://api.anthropic.com/v1/messages" }, client, HttpMethod.Post
    ) {
    override suspend fun parseResult(
        request: ClaudeGenerationRequest,
        response: JSONObject
    ): Either<APIError, ClaudeGenerationResponse> {
        return ClaudeGenerationResponse.fromJson(response).right()
    }

    override fun HeadersBuilder.applyCredential() {
        append("x-api-key", apiKey)
        append("anthropic-version", "2023-06-01")
    }
}