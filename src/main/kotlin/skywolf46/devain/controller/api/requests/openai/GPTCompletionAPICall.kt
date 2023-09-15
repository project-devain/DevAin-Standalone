package skywolf46.devain.controller.api.requests.openai

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.parser.JSONParser
import org.koin.core.component.get
import skywolf46.devain.KEY_GPT_PROCEED_COUNT
import skywolf46.devain.KEY_GPT_PROCEED_TIME
import skywolf46.devain.KEY_GPT_PROCEED_TOKEN
import skywolf46.devain.controller.api.APICall
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.error.PreconditionError
import skywolf46.devain.controller.api.error.StandardRestAPIError
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.model.rest.devain.data.request.UpdateRequest
import skywolf46.devain.model.rest.gpt.completion.request.OpenAIGPTRequest
import skywolf46.devain.model.rest.gpt.completion.response.OpenAIGPTResponse
import skywolf46.devain.util.parseMap

private const val OPENAI_GPT_COMPLETION_ENDPOINT = "https://api.openai.com/v1/chat/completions"

class GPTCompletionAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    APICall<OpenAIGPTRequest, OpenAIGPTResponse> {
    private val client = client.getOrElse { get() }
    private val parser = get<JSONParser>()
    private val updateCall = get<DevAinUpdatePersistenceCountAPICall>()

    override suspend fun call(request: OpenAIGPTRequest): Either<APIError, OpenAIGPTResponse> {
        return runCatching {
            val prebuiltRequest = request.asJson().getOrElse { return PreconditionError(it).left() }
            val result = client.post(OPENAI_GPT_COMPLETION_ENDPOINT) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "Bearer $apiKey")
                }
                setBody(prebuiltRequest.toJSONString())
                println(prebuiltRequest.toJSONString())
            }
            if (result.status.value != 200) {
                return StandardRestAPIError(result.status.value, result.bodyAsText()).left()
            }
            OpenAIGPTResponse.fromJson(parser.parseMap(result.bodyAsText())).apply {
                updateCall.call(UpdateRequest(KEY_GPT_PROCEED_COUNT, 1L))
                updateCall.call(UpdateRequest(KEY_GPT_PROCEED_TOKEN, usage.completionToken.toLong()))
                updateCall.call(UpdateRequest(KEY_GPT_PROCEED_TIME, (System.currentTimeMillis() - request.createdOn)))
            }.right()
        }.getOrElse {
            UnexpectedError(it).left()
        }
    }
}