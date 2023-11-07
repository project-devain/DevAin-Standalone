package skywolf46.devain.model.api.openai

import arrow.core.None
import arrow.core.Option
import arrow.core.toOption
import org.json.simple.JSONObject

class OpenAIImage(val url: Option<String> = None, val base64: Option<String> = None) {
    companion object {
        fun fromJson(data: JSONObject): OpenAIImage {
            if ("url" in data)
                return OpenAIImage(url = data["url"].toString().toOption())
            if ("b64_json" in data)
                return OpenAIImage(url = data["b64_json"].toString().toOption())
            throw IllegalArgumentException("Invalid image response")
        }
    }
}