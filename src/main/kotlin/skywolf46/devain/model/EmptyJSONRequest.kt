package skywolf46.devain.model

import arrow.core.Either
import arrow.core.right
import org.json.simple.JSONObject

open class EmptyJSONRequest : Request<JSONObject> {
    override fun asJson(): Either<Throwable, JSONObject> {
        return JSONObject().right()
    }
}