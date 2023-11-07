package skywolf46.devain.controller.api.requests.google

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.koin.core.component.get
import org.koin.core.component.inject
import skywolf46.devain.controller.api.APICall
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.error.PreconditionError
import skywolf46.devain.controller.api.error.StandardRestAPIError
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.model.api.google.GoogleSearchRequest
import skywolf46.devain.model.api.google.GoogleSearchResponse
import java.net.URLEncoder

private const val GOOGLE_SEARCH_ENDPOINT = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&num=%d&q=%s"

class GoogleSearchAPICall(private val apiKey: String, private val engineId: String, client: Option<HttpClient> = None) :
    APICall<GoogleSearchRequest, GoogleSearchResponse> {
    private val apiClient by lazy {
        client.getOrElse { get<HttpClient>() }
    }
    private val parser by inject<JSONParser>()

    override suspend fun call(request: GoogleSearchRequest): Either<APIError, GoogleSearchResponse> {
        return Either.catch {
            val result = apiClient.get(
                GOOGLE_SEARCH_ENDPOINT.format(
                    apiKey,
                    engineId,
                    request.amount,
                    URLEncoder.encode(request.query, "UTF-8")
                )
            ) {
                contentType(ContentType.Application.Json)
            }
            if (result.status.value != 200) {
                StandardRestAPIError(result.status.value, result.bodyAsText()).left()
            } else {
                GoogleSearchResponse.fromJson(parser.parse(result.bodyAsText()) as JSONObject).right()
            }
        }.getOrElse {
            UnexpectedError(it).left()
        }
    }
}