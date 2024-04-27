package skywolf46.devain.model.api.anthropic

import arrow.core.Either
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Request

class ClaudeMessage(val type: ClaudeGenerationRole, val content: String) : Request<JSONObject> {

    enum class ClaudeGenerationRole(val type: String) {
        USER("user"), ASSISTANT("assistant")
    }

    override fun serialize(): Either<Throwable, JSONObject> {
        return JSONObject().apply {
            this["role"] = type.type
            this["content"] = content
        }.right()
    }
}