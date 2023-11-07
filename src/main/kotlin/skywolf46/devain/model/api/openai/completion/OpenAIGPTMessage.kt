package skywolf46.devain.model.api.openai.completion

import arrow.core.*
import org.json.simple.JSONObject
import skywolf46.devain.model.Request

open class OpenAIGPTMessage(
    /**
     * Message role.
     */
    val role: Role,
    /**
     * Message content.
     */
    val content: Option<String>,
    val functionCall: Option<JSONObject> = None,
    val functionName: Option<String> = None
) : Request<JSONObject> {
    companion object {

        /**
         * Parse OpenAI response from JSON.
         */
        fun fromJson(data: JSONObject): OpenAIGPTMessage {
            return OpenAIGPTMessage(
                Role.valueOf(data["role"].toString().uppercase()),
                data["content"]?.toString().toOption(),
                (data["function_call"] as? JSONObject).toOption(),
                data["name"]?.toString().toOption(),
            )
        }
    }

    override fun asJson(): Either<Throwable, JSONObject> {
        return JSONObject().apply {

            this["role"] = role.gptType.lowercase()
            this["content"] = content.orNull()
            functionCall.tap {
                this["function_call"] = it
            }
            functionName.tap {
                this["name"] = it
            }

        }.right()
    }

    enum class Role(val gptType: String) {

        /**
         * Pre-assigned system prompt
         */
        SYSTEM("system"),

        FUNCTION("function"),

        IMAGE_URL("image_url"),

        /**
         * User input based precondition input.
         */
        USER_PRECONDITION("user"),

        /**
         * User input.
         */
        USER("user"),

        /**
         * AI output.
         */
        ASSISTANT("assistant"),

    }
}