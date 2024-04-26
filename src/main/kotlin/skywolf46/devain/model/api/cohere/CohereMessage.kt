package skywolf46.devain.model.api.cohere

import org.json.simple.JSONObject

data class CohereMessage(val id: String, val text: String) {
    companion object {
        fun fromJson(data: JSONObject) : CohereMessage {
            return CohereMessage(
                data["id"] as String,
                data["text"] as String
            )
        }
    }
}