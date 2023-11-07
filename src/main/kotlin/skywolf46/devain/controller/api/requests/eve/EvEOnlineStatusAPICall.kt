package skywolf46.devain.controller.api.requests.eve

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import io.ktor.client.*
import org.json.simple.JSONObject
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.RESTAPICall
import skywolf46.devain.model.EmptyJSONRequest
import skywolf46.devain.model.GenericJSONObjectResponse

class EvEOnlineStatusAPICall(client: Option<HttpClient> = None) :
    RESTAPICall<EmptyJSONRequest, GenericJSONObjectResponse>({ "https://esi.evetech.net/latest/status/" }, client) {
    override suspend fun parseResult(
        request: EmptyJSONRequest,
        response: JSONObject
    ): Either<APIError, GenericJSONObjectResponse> {
        return GenericJSONObjectResponse(response).right()
    }

}