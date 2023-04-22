package skywolf46.devain.data.parsed.gpt

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class ParsedGPTChoiceData(data: JSONArray) : ArrayList<ParsedGPTAnswer>() {
    init {
        for (x in data) {
            add(ParsedGPTAnswer(x as JSONObject))
        }
    }
}