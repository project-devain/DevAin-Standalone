package skywolf46.devain.model

import arrow.core.Either
import org.json.simple.JSONObject

interface Request<T: Any> {
    fun asJson() : Either<Throwable, T>
}