package skywolf46.devain.model.rest.gpt.completion.response

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import skywolf46.devain.model.Response

data class OpenAIGPTResponse(
    /**
     * Message ID from OpenAI call.
     */
    val chatId: String,
    /**
     * Model used for request.
     */
    val model: String,
    /**
     * Timestamp of request.
     */
    val timeStamp: Long,
    /**
     * Objective of request.
     */
    val objective: String,
    /**
     * Model used for request.
     */
    val usage: OpenAIGPTTokenUsage,
    /**
     * Request result.
     */
    val answers: List<OpenAIGPTAnswer>
) : Response {
    companion object {
        /**
         * Parse OpenAI response from JSON.
         */
        fun fromJson(data: JSONObject): OpenAIGPTResponse {
            return OpenAIGPTResponse(
                data["id"].toString(),
                data["model"].toString(),
                data["created"].toString().toLong(),
                data["object"].toString(),
                OpenAIGPTTokenUsage.fromJson(data["usage"] as JSONObject),
                (data["choices"] as JSONArray).map { OpenAIGPTAnswer.fromJson(it as JSONObject) }
            )
        }
    }

}