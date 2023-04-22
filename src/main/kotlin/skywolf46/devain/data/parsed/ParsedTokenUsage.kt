package skywolf46.devain.data.parsed

import org.json.simple.JSONObject

class ParsedTokenUsage(data: JSONObject) {
    val promptTokens = data["prompt_tokens"].toString().toInt()
    val completionTokens = data["completion_tokens"].toString().toInt()
    val totalTokens = data["total_tokens"].toString().toInt()
}