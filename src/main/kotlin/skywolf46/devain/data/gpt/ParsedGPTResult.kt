package skywolf46.devain.data.gpt

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import skywolf46.devain.data.ParsedTokenUsage

class ParsedGPTResult(data: JSONObject) {
    val chatId = data["id"].toString()
    val objectId = data["object"].toString()
    val timeStamp = data["created"].toString().toLong()
    val modelId = data["mode"].toString()
    val tokenUsage = ParsedTokenUsage(data["usage"] as JSONObject)
    val choices = ParsedGPTChoiceData(data["choices"] as JSONArray)
}