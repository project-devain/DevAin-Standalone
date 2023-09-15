package skywolf46.devain.controller.api

import arrow.core.Either
import org.koin.core.component.KoinComponent
import skywolf46.devain.model.EmptyRequest
import skywolf46.devain.model.Request
import skywolf46.devain.model.Response

interface APICall<REQUEST : Request<*>, RESPONSE : Response> : KoinComponent {
    suspend fun call(request: REQUEST): Either<APIError, RESPONSE>
}

suspend inline fun <V : Response> APICall<EmptyRequest, V>.call(): Either<APIError, V> {
    return call(EmptyRequest())
}

suspend inline fun <REQUEST : Request<*>, RESPONSE : Response> APICall<REQUEST, RESPONSE>.certainly(request: REQUEST): RESPONSE {
    return call(request).getOrNull()!!
}


suspend inline fun <RESPONSE : Response> APICall<EmptyRequest, RESPONSE>.certainly(): RESPONSE {
    return call(EmptyRequest()).getOrNull()!!
}