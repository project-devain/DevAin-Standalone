package skywolf46.devain.model.api.google

import org.json.simple.JSONObject
import skywolf46.devain.apicall.networking.Response
import skywolf46.devain.util.getList

class GoogleSearchResponse(
    val kind: String,
    val items: List<GoogleSearchItem>
) : Response {
    companion object {
        fun fromJson(data: JSONObject): GoogleSearchResponse {
            return GoogleSearchResponse(
                data["kind"] as String,
                data.getList("items").map { GoogleSearchItem.fromJson(it as JSONObject) }
            )
        }
    }
}