package skywolf46.devain.controller.api.requests.deepl

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.parser.JSONParser
import org.koin.core.component.get
import skywolf46.devain.KEY_DEEPL_PROCEED_COUNT
import skywolf46.devain.KEY_DEEPL_PROCEED_TOKEN
import skywolf46.devain.controller.api.APICall
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.error.PreconditionError
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.controller.api.requests.devain.DevAinUpdatePersistenceCountAPICall
import skywolf46.devain.model.rest.deepl.translation.DeepLTranslateRequest
import skywolf46.devain.model.rest.deepl.translation.DeepLTranslateResponse
import skywolf46.devain.model.rest.devain.data.request.UpdateRequest
import skywolf46.devain.util.getList
import skywolf46.devain.util.getMap
import skywolf46.devain.util.parseMap

private const val DEEPL_FREE_TRANSLATION_ENDPOINT = "https://api-free.deepl.com/v2/translate"
private const val DEEPL_PRO_TRANSLATION_ENDPOINT = "https://api.deepl.com/v2/translate"

class DeepLTranslationAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    APICall<DeepLTranslateRequest, DeepLTranslateResponse> {
    private val targetEndpointURL =
        if (apiKey.endsWith(":fx")) DEEPL_FREE_TRANSLATION_ENDPOINT else DEEPL_PRO_TRANSLATION_ENDPOINT
    private val client = client.getOrElse { get() }
    private val parser = get<JSONParser>()
    private val updateCall = get<DevAinUpdatePersistenceCountAPICall>()


    override suspend fun call(request: DeepLTranslateRequest): Either<APIError, DeepLTranslateResponse> {
        return runCatching {
            val prebuiltRequest = request.asJson().getOrElse { return PreconditionError(it).left() }
            val result = client.post(targetEndpointURL) {
                contentType(ContentType.Application.Json)
                headers {
                    append("Authorization", "DeepL-Auth-Key $apiKey")
                }
                setBody(prebuiltRequest.toJSONString())
            }
            val resultMap = parser.parseMap(result.bodyAsText())
            if ("translations" !in resultMap)
                return UnexpectedError(IllegalStateException("Translation not found in response (${resultMap})")).left()
            DeepLTranslateResponse.fromJson(resultMap.getList("translations").getMap(0))
                .apply {
                    updateCall.call(UpdateRequest(KEY_DEEPL_PROCEED_COUNT, 1L))
                    updateCall.call(UpdateRequest(KEY_DEEPL_PROCEED_TOKEN, translationResult.length.toLong()))
                }.right()
        }.getOrElse {
            UnexpectedError(it).left()
        }
    }
}