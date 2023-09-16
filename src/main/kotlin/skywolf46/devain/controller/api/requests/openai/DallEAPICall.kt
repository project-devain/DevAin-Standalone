package skywolf46.devain.controller.api.requests.openai

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.parser.JSONParser
import org.koin.core.component.get
import skywolf46.devain.KEY_DALLE_PROCEED_COUNT
import skywolf46.devain.KEY_DALLE_PROCEED_TIME
import skywolf46.devain.KEY_GPT_PROCEED_TIME
import skywolf46.devain.controller.api.APICall
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.error.PreconditionError
import skywolf46.devain.controller.api.error.StandardRestAPIError
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.model.rest.devain.data.request.UpdateRequest
import skywolf46.devain.model.rest.openai.dalle.DallERequest
import skywolf46.devain.model.rest.openai.dalle.DallEResponse
import skywolf46.devain.util.parseMap

private const val OPENAI_DALLE_COMPLETION_ENDPOINT = "https://api.openai.com/v1/images/generations"

class DallEAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    APICall<DallERequest, DallEResponse> {
    private val client = client.getOrElse { get() }
    private val parser = get<JSONParser>()
    private val updateCall = get<DevAinUpdatePersistenceCountAPICall>()
    override suspend fun call(request: DallERequest): Either<APIError, DallEResponse> {
        return runCatching {
            val prebuiltRequest = request.asJson().getOrElse { return PreconditionError(it).left() }
            val result = client.post(OPENAI_DALLE_COMPLETION_ENDPOINT) {
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
            DallEResponse.fromJson(parser.parseMap(result.bodyAsText())).apply {
                updateCall.call(UpdateRequest(KEY_DALLE_PROCEED_COUNT, 1L))
                updateCall.call(UpdateRequest(KEY_DALLE_PROCEED_TIME, (System.currentTimeMillis() - request.createdOn)))
            }.right()
        }.getOrElse {
            UnexpectedError(it).left()
        }
    }

}