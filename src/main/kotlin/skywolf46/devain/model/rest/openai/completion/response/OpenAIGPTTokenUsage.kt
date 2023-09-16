package skywolf46.devain.model.rest.openai.completion.response

import org.json.simple.JSONObject

data class OpenAIGPTTokenUsage(
    /**
     * Prompt(Input) token count.
     */
    val promptToken: Int,
    /**
     * Completion(Output) token count.
     */
    val completionToken: Int,
    /**
     * Total token count.
     */
    val totalToken: Int
) {
    companion object {
        /**
         * Parse OpenAI response from JSON.
         */
        fun fromJson(data: JSONObject): OpenAIGPTTokenUsage {
            return OpenAIGPTTokenUsage(
                data["prompt_tokens"].toString().toInt(),
                data["completion_tokens"].toString().toInt(),
                data["total_tokens"].toString().toInt()
            )
        }
    }
}