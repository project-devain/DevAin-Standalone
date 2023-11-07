package skywolf46.devain.controller.api.cohere

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.koin.core.component.inject
import skywolf46.devain.KEY_COHERE_GENERATION_PROCEED_COUNT
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.RESTAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.model.api.cohere.CohereGenerationRequest
import skywolf46.devain.model.api.cohere.CohereGenerationResponse
import skywolf46.devain.model.api.openai.UpdateRequest

class CohereGenerationAPICall(val apiKey: String, client: Option<HttpClient> = None) :
    RESTAPICall<CohereGenerationRequest, CohereGenerationResponse>({
        "https://api.cohere.ai/v1/generate"
    }, client, HttpMethod.Post) {

    private val updateCall by inject<DevAinUpdatePersistenceCountAPICall>()

    override suspend fun parseResult(
        request: CohereGenerationRequest,
        response: JSONObject
    ): Either<APIError, CohereGenerationResponse> {
        return CohereGenerationResponse.fromJson(response).apply {
            updateCall.call(UpdateRequest(KEY_COHERE_GENERATION_PROCEED_COUNT, 1L))
        }.right()
    }

    override fun HeadersBuilder.applyCredential() {
        append("authorization", "Bearer $apiKey")
    }

    override suspend fun parseHttpError(
        request: CohereGenerationRequest,
        response: HttpResponse,
        errorCode: Int
    ): APIError {
        println(response.headers)
        return super.parseHttpError(request, response, errorCode)
    }
}