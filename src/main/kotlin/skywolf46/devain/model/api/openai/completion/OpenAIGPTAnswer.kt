package skywolf46.devain.model.api.openai.completion

import org.json.simple.JSONObject

data class OpenAIGPTAnswer(
    /**
     * Response finish flag.
     */
    val reason: FinishReason,
    /**
     * Answer index.
     */
    val index: Int,
    /**
     * Answer contents.
     */
    val message: OpenAIGPTMessage,
) {
    companion object {
        /**
         * Parse OpenAI response from JSON.
         */
        fun fromJson(data: JSONObject): OpenAIGPTAnswer {
            return OpenAIGPTAnswer(
                FinishReason.valueOf(data["finish_reason"].toString().uppercase()),
                data["index"].toString().toInt(),
                OpenAIGPTMessage.fromJson(data["message"] as JSONObject)
            )
        }
    }

    enum class FinishReason {
        /**
         * Fully generated response.
         */
        STOP,

        /**
         * Response is generated, but it's limited by max length.
         */
        LENGTH,

        /**
         * Model decided to call function.
         */
        FUNCTION_CALL,

        /**
         * Content filtered with OpenAI restriction.
         */
        CONTENT_FILTER,

        /**
         * Unknown reason.
         * Answer is generating, or imcompleted, or else.
         */
        NULL
    }
}
