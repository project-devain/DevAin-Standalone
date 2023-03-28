package skywolf46.devain.data.edit

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import skywolf46.devain.data.gpt.ParsedGPTChoiceData
import skywolf46.devain.data.ParsedTokenUsage

class ParsedEditResult(data: JSONObject) {
    val objective = data["object"].toString()
    val timeStamp = data["created"].toString().toLong()
    val usage = ParsedTokenUsage(data["usage"] as JSONObject)
    val choices = ParsedEditChoiceData(data["choices"] as JSONArray)
}