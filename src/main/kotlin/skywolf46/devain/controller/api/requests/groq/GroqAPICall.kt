package skywolf46.devain.controller.api.requests.groq

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.koin.core.component.inject
import skywolf46.devain.KEY_GROQ_GENERATION_PROCEED_COUNT
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.RESTAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.model.api.openai.UpdateRequest
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionCallStackTrace
import skywolf46.devain.model.api.openai.completion.OpenAIGPTRequest
import skywolf46.devain.model.api.openai.completion.OpenAIGPTResponse

class GroqAPICall(val apiKey: String, client: Option<HttpClient> = None) :
    RESTAPICall<OpenAIGPTRequest, OpenAIGPTResponse>({
        "https://api.groq.com/openai/v1/chat/completions"
    }, client, HttpMethod.Post) {
    private val updateCall by inject<DevAinUpdatePersistenceCountAPICall>()

    override suspend fun parseResult(
        request: OpenAIGPTRequest,
        response: JSONObject
    ): Either<APIError, OpenAIGPTResponse> {
        return OpenAIGPTResponse.fromJson(response, OpenAIFunctionCallStackTrace()).apply {
            updateCall.call(UpdateRequest(KEY_GROQ_GENERATION_PROCEED_COUNT, 1L))
        }.right()
    }

    override fun HeadersBuilder.applyCredential() {
        append("authorization", "Bearer $apiKey")
    }

    override suspend fun parseHttpError(
        request: OpenAIGPTRequest,
        response: HttpResponse,
        errorCode: Int
    ): APIError {
        println(response.headers)
        return super.parseHttpError(request, response, errorCode)
    }
}