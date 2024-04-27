package skywolf46.devain.model.api.cohere

import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Response
import skywolf46.devain.util.getList

data class CohereGenerationResponse(val id: String, val prompt: String, val generations: List<CohereMessage>) :
    Response {
    companion object {
        fun fromJson(data: JSONObject): CohereGenerationResponse {
            return CohereGenerationResponse(
                data["id"] as String,
                data["prompt"] as String,
                data.getList("generations").map { CohereMessage.fromJson(it as JSONObject) }
            )
        }
    }
}