package skywolf46.devain.model.api.anthropic

import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Response
import skywolf46.devain.util.getList

class ClaudeGenerationResponse(
    val message: List<String>
) : Response {
    companion object {
        fun fromJson(data: JSONObject): ClaudeGenerationResponse {
            if ("content" in data) {
                val list = data.getList("content")
                return ClaudeGenerationResponse(list.map { (it as JSONObject)["text"].toString() })
            }
            return ClaudeGenerationResponse(listOf())
        }
    }
}