package skywolf46.devain.model.api.rplus

import arrow.core.Either
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Request

class RPlusMessageHistory(
    val role: RPlusMessageType,
    val message: String
) : Request<JSONObject> {
    override fun serialize(): Either<Throwable, JSONObject> {
        return JSONObject().apply {
            this["role"] = role.name
            this["message"] = message
        }.right()
    }

    enum class RPlusMessageType {
        USER, SYSTEM, CHATBOT
    }

}