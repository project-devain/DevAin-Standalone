package skywolf46.devain.model.rest.gpt.completion

import arrow.core.Either
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.model.Request

data class OpenAIGPTMessage(
    /**
     * Message role.
     */
    val role: Role,
    /**
     * Message content.
     */
    val content: String
) : Request<JSONObject> {
    companion object {

        /**
         * Parse OpenAI response from JSON.
         */
        fun fromJson(data: JSONObject): OpenAIGPTMessage {
            return OpenAIGPTMessage(Role.valueOf(data["role"].toString().uppercase()), data["content"] as String)
        }
    }

    override fun asJson(): Either<Throwable, JSONObject> {
        return JSONObject(
            mutableMapOf(
                "role" to role.name.lowercase(),
                "content" to content
            )
        ).right()
    }

    enum class Role {

        /**
         * Pre-assigned system prompt
         */
        SYSTEM,

        /**
         * User input.
         */
        USER,

        /**
         * AI output.
         */
        ASSISTANT
    }
}