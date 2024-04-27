package skywolf46.devain.model.api.openai.completion

import arrow.core.*
import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Request

open class OpenAIGPTMessage(
    /**
     * Message role.
     */
    val role: Role,
    /**
     * Message content.
     */
    val content: List<Pair<String, String>>,
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

        operator fun invoke(
            /**
             * Message role.
             */
            role: Role,
            /**
             * Message content.
             */
            content: Option<String>,
            functionCall: Option<JSONObject> = None,
            functionName: Option<String> = None
        ) = OpenAIGPTMessage(
            role,
            if (content.isNone()) emptyList() else listOf("text" to content.getOrNull()!!),
            functionCall,
            functionName
        )
    }

    override fun serialize(): Either<Throwable, JSONObject> {
        return JSONObject().apply {

            this["role"] = role.gptType.lowercase()
            this["content"] = content.map {
                JSONObject().apply {
                    this["type"] = it.first
                    when (this["type"]) {
                        "image_url" -> this["image_url"] = it.second
                        else -> this["text"] = it.second
                    }
                }
            }
            functionCall.onSome {
                this["function_call"] = it
            }
            functionName.onSome {
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