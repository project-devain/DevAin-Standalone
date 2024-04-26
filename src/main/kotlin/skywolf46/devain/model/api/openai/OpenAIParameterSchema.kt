package skywolf46.devain.model.api.openai

import arrow.core.Either
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Request

class OpenAIParameterSchema(
    val parameterName: String,
    val description: String,
    val type: String,
    val required: String
) : Request<JSONObject> {
    override fun serialize(): Either<Throwable, JSONObject> {
        return JSONObject()
            .apply {
                put("type", type)
                put("description", description)
            }
            .right()
    }
}