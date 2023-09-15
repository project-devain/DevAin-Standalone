package skywolf46.devain.controller.api.requests.deepl

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.parser.JSONParser
import org.koin.core.component.get
import skywolf46.devain.controller.api.APICall
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.error.PreconditionError
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.model.rest.deepl.translation.DeepLTranslateRequest
import skywolf46.devain.model.rest.deepl.translation.DeepLTranslateResponse
import skywolf46.devain.util.parseMap

private const val DEEPL_FREE_TRANSLATION_ENDPOINT = "https://api-free.deepl.com/v2/translate"
private const val DEEPL_PRO_TRANSLATION_ENDPOINT = "https://api.deepl.com/v2/translate"

class DeepLTranslationAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    APICall<DeepLTranslateRequest, DeepLTranslateResponse> {
    private val targetEndpointURL =
        if (apiKey.endsWith(":fx")) DEEPL_FREE_TRANSLATION_ENDPOINT else DEEPL_PRO_TRANSLATION_ENDPOINT
    private val client = client.getOrElse { get() }
    private val parser = get<JSONParser>()

    override suspend fun call(request: DeepLTranslateRequest): Either<APIError, DeepLTranslateResponse> {
        val prebuiltRequest = request.asJson().getOrElse { return PreconditionError(it).left() }
        val result = client.post(targetEndpointURL) {
            contentType(ContentType.Application.Json)
            headers {
                append("Authorization", "DeepL-Auth-Key $apiKey")
            }
            setBody(prebuiltRequest.toJSONString())
        }
        return runCatching {
            DeepLTranslateResponse.fromJson(parser.parseMap(result.bodyAsText())).right()
        }.getOrElse {
            UnexpectedError(it).left()
        }
    }
}