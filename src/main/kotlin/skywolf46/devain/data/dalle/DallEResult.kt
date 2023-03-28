package skywolf46.devain.data.dalle

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class DallEResult(data: JSONObject){
    val timeStamp = data["created"].toString().toLong()
    val urls = (data["data"] as JSONArray).map {
        (it as JSONObject)["url"].toString()
    }
}