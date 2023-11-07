package skywolf46.devain.controller.api

import arrow.core.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.koin.core.component.get
import org.koin.core.component.inject
import skywolf46.devain.controller.api.error.PreconditionError
import skywolf46.devain.controller.api.error.StandardRestAPIError
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.model.Request
import skywolf46.devain.model.Response

abstract class RESTAPICall<REQUEST : Request<JSONObject>, RESPONSE : Response>(
    val endpointProvider: (REQUEST) -> String,
    client: Option<HttpClient> = None,
    val apiMethod: HttpMethod = HttpMethod.Get
) : APICall<REQUEST, RESPONSE> {

    private val client by lazy {
        client.getOrElse { get<HttpClient>() }
    }
    private val jsonParser by inject<JSONParser>()

    override suspend fun call(request: REQUEST): Either<APIError, RESPONSE> {
        val prebuiltRequest = request.asJson().getOrElse { return PreconditionError(it).left() }
        println(endpointProvider(request))
        println(prebuiltRequest)
        return Either.catch {
            val result = client.request(endpointProvider(request)) {
                method = apiMethod
                contentType(ContentType.Application.Json)
                headers {
                    applyCredential()
                }
                setBody(prebuiltRequest.toJSONString())
            }
            if (result.status.value != 200) {
                parseHttpError(request, result, result.status.value).left()
            } else {
                parseResult(request, jsonParser.parse(result.bodyAsText()) as JSONObject)
            }
        }.getOrElse {
            onError(it).left()
        }
    }

    protected abstract suspend fun parseResult(request: REQUEST, response: JSONObject): Either<APIError, RESPONSE>

    protected open suspend fun parseHttpError(request: REQUEST, response: HttpResponse, errorCode: Int): APIError {
        return StandardRestAPIError(errorCode, response.bodyAsText())
    }

    protected open suspend fun onError(throwable: Throwable): APIError {
        return UnexpectedError(throwable)
    }

    protected open fun HeadersBuilder.applyCredential() {
        // Do nothing
    }
}