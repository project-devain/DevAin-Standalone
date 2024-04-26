package skywolf46.devain.controller.api.requests.deepl

import arrow.core.*
import io.ktor.client.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.koin.core.component.get
import skywolf46.devain.KEY_DEEPL_PROCEED_COUNT
import skywolf46.devain.KEY_DEEPL_PROCEED_TOKEN
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.RESTAPICall
import skywolf46.devain.apicall.errors.UnexpectedError
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.model.api.deepl.translation.DeepLTranslateRequest
import skywolf46.devain.model.api.deepl.translation.DeepLTranslateResponse
import skywolf46.devain.model.api.openai.UpdateRequest
import skywolf46.devain.util.getList
import skywolf46.devain.util.getMap


class DeepLTranslationAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    RESTAPICall<DeepLTranslateRequest, DeepLTranslateResponse>({
        if (apiKey.endsWith(":fx"))
        // DeepL Free API Endpoint
            "https://api-free.deepl.com/v2/translate"
        else
        // DeepL Pro API Endpoint
            "https://api.deepl.com/v2/translate"
    }, client) {
    private val updateCall = get<DevAinUpdatePersistenceCountAPICall>()

    override suspend fun parseResult(
        request: DeepLTranslateRequest,
        response: JSONObject
    ): Either<APIError, DeepLTranslateResponse> {
        if ("translations" !in response)
            return UnexpectedError(IllegalStateException("Translation not found in response (${response})")).left()
        return DeepLTranslateResponse.fromJson(response.getList("translations").getMap(0))
            .apply {
                updateCall.call(UpdateRequest(KEY_DEEPL_PROCEED_COUNT, 1L))
                updateCall.call(UpdateRequest(KEY_DEEPL_PROCEED_TOKEN, translationResult.length.toLong()))
            }.right()
    }

    override fun HeadersBuilder.applyCredential() {
        append("Authorization", "DeepL-Auth-Key $apiKey")
    }
}