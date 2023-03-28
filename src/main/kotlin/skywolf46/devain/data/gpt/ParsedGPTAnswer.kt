package skywolf46.devain.data.gpt

import org.json.simple.JSONObject

class ParsedGPTAnswer(data: JSONObject) {
    val answer = (data["message"] as JSONObject)["content"].toString().trimStart('\n')
}