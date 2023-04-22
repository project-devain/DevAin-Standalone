package skywolf46.devain.data.parsed.edit

import org.json.simple.JSONObject

class ParsedEditAnswer(data: JSONObject) {
    val result = data["text"].toString().trimStart('\n')
}