package skywolf46.devain.data.parsed.dreamstudio

import org.json.simple.JSONObject

class DreamStudioError(data: JSONObject) {
    val name: String
    val message: String

    init {
        name = data["name"].toString()
        message = data["message"].toString()
    }
}