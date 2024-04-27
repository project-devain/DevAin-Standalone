package skywolf46.devain.controller.api.requests.openai

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import io.ktor.client.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.koin.core.component.get
import skywolf46.devain.KEY_DALLE_PROCEED_COUNT
import skywolf46.devain.KEY_DALLE_PROCEED_TIME
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.RESTAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.model.api.openai.UpdateRequest
import skywolf46.devain.model.api.openai.dalle.DallERequest
import skywolf46.devain.model.api.openai.dalle.DallEResponse

class DallEAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    RESTAPICall<DallERequest, DallEResponse>({
        "https://api.openai.com/v1/images/generations"
    }, client, HttpMethod.Post) {
    private val updateCall = get<DevAinUpdatePersistenceCountAPICall>()

    override suspend fun parseResult(request: DallERequest, response: JSONObject): Either<APIError, DallEResponse> {
        return DallEResponse.fromJson(response).apply {
            updateCall.call(UpdateRequest(KEY_DALLE_PROCEED_COUNT, 1L))
            updateCall.call(UpdateRequest(KEY_DALLE_PROCEED_TIME, (System.currentTimeMillis() - request.createdOn)))
        }.right()
    }

    override fun HeadersBuilder.applyCredential() {
        append("Authorization", "Bearer $apiKey")
    }
}