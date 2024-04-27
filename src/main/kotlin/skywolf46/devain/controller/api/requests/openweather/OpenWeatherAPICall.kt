package skywolf46.devain.controller.api.requests.openweather

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import io.ktor.client.*
import org.json.simple.JSONObject
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.RESTAPICall
import skywolf46.devain.apicall.networking.GenericJSONObjectResponse
import skywolf46.devain.apicall.networking.GetRequest

class OpenWeatherAPICall(private val apiKey: String, client: Option<HttpClient> = None) :
    RESTAPICall<GetRequest<String>, GenericJSONObjectResponse>({
        "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s".format(it.key, apiKey)
    }, client) {

    override suspend fun parseResult(
        request: GetRequest<String>,
        response: JSONObject
    ): Either<APIError, GenericJSONObjectResponse> {
        return GenericJSONObjectResponse(response).right()
    }
}