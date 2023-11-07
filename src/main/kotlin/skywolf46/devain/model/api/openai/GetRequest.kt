package skywolf46.devain.model.api.openai

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.model.Request

data class GetRequest<T : Any>(val key: String, val section: Option<String> = None) : Request<JSONObject> {
    override fun asJson(): Either<Throwable, JSONObject> {
        return JSONObject().right()
    }
}