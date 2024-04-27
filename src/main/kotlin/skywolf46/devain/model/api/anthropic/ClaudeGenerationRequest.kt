package skywolf46.devain.model.api.anthropic

import arrow.core.Either
import arrow.core.Option
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Request

class ClaudeGenerationRequest(
    val model: String,
    val messages: List<ClaudeMessage>,
    val maxTokens: Option<Int>,
) : Request<JSONObject> {
    override fun serialize(): Either<Throwable, JSONObject> {
        val obj = JSONObject()
        obj["model"] = model
        obj["messages"] = messages.map { it.serialize().getOrNull() }
        maxTokens.map { obj["max_tokens"] = it }
        return obj.right()
    }

}