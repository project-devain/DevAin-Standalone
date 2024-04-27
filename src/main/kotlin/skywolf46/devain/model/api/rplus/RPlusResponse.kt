package skywolf46.devain.model.api.rplus

import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Response

class RPlusResponse(val text: String) : Response {
    companion object {
        fun fromJson(jsonObject: JSONObject): RPlusResponse {
            return RPlusResponse(
                jsonObject["text"].toString()
            )
        }
    }
}