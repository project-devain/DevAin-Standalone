package skywolf46.devain.model.api.deepl.translation

import org.json.simple.JSONObject
import skywolf46.devain.model.Response

data class DeepLTranslateResponse(val sourceLanguage: String, val translationResult: String) : Response {
    companion object {
        fun fromJson(data: JSONObject): DeepLTranslateResponse {
            return DeepLTranslateResponse(
                data["detected_source_language"].toString(),
                data["text"].toString()
            )
        }
    }
}