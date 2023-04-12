package skywolf46.devain.data.dreamstudio

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class DreamStudioResponse(data: JSONObject) {
    val base64Image: String?
    val finishReason: String
    val seed: Long

    init {
        val result = ((data["artifacts"] as JSONArray)[0] as JSONObject)
        base64Image = result["base64"]?.toString()
        finishReason = result["finishReason"].toString()
        seed = result["seed"]?.toString()?.toLong() ?: 0
    }
}