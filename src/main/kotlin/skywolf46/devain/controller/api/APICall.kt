package skywolf46.devain.controller.api

import arrow.core.Either
import org.koin.core.component.KoinComponent
import skywolf46.devain.model.Request
import skywolf46.devain.model.Response

interface APICall<REQUEST : Request<*>, RESPONSE : Response> : KoinComponent {
    suspend fun call(request: REQUEST): Either<APIError, RESPONSE>
}