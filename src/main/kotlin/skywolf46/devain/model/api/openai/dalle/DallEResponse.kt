package skywolf46.devain.model.api.openai.dalle

import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Response
import skywolf46.devain.model.api.openai.OpenAIImage
import skywolf46.devain.util.getList

data class DallEResponse(val timeStamp: Long, val images: List<OpenAIImage>) : Response {
    companion object {
        fun fromJson(data: JSONObject): DallEResponse {
            return DallEResponse(data["created"].toString().toLong(), data.getList("data").map {
                OpenAIImage.fromJson(it as JSONObject)
            })
        }
    }
}