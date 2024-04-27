package skywolf46.devain.controller.api.requests.devain

import arrow.core.Either
import arrow.core.right
import skywolf46.devain.apicall.APICall
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.networking.EmptyRequest
import skywolf46.devain.apicall.networking.GenericMapResponse
import java.util.*

class DevAinAppPropertiesAPICall : APICall<EmptyRequest, GenericMapResponse<String, String>> {
    private val properties = GenericMapResponse<String, String>()

    init {
        properties.putAll(javaClass.getResourceAsStream("/devain.properties").use {
            Properties().also { prop ->
                prop.load(it)
            }
        }.map { it.key.toString() to it.value.toString() })
    }

    override suspend fun call(request: EmptyRequest): Either<APIError, GenericMapResponse<String, String>> {
        return properties.clone().right()
    }
}