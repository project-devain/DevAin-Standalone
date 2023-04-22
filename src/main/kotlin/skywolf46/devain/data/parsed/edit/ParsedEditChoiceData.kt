package skywolf46.devain.data.parsed.edit

import org.json.simple.JSONArray
import org.json.simple.JSONObject

class ParsedEditChoiceData(data: JSONArray) : ArrayList<ParsedEditAnswer>() {
    init {
        for (x in data) {
            add(ParsedEditAnswer(x as JSONObject))
        }
    }
}