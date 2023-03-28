package skywolf46.devain.data.edit

import org.json.simple.JSONObject

class ParsedEditAnswer(data: JSONObject) {
    val result = data["text"].toString().trimStart('\n')
}