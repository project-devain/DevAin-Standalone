package skywolf46.devain.model.rest.deepl.translation

import arrow.core.Either
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.model.Request
import skywolf46.devain.util.putArray

class DeepLTranslateRequest(
    val sourceLanguage: Option<String>,
    val targetLanguage: String,
    val text: String
) :
    Request<JSONObject> {
    companion object {

        private val supportedLanguage = mapOf(
            "Bulgarian" to "BG",
            "Czech" to "CZ",
            "Danish" to "DA",
            "German" to "DE",
            "Greek" to "EL",
            "English" to "EN",
            "Spanish" to "ES",
            "Estonian" to "ET",
            "Finnish" to "FI",
            "French" to "FR",
            "Hungarian" to "HU",
            "Indonesian" to "ID",
            "Italian" to "IT",
            "Japanese" to "JA",
            "Korean" to "KO",
            "Lithuanian" to "LT",
            "Latvian" to "LV",
            "Norwegian" to "NB",
            "Dutch" to "NL",
            "Polish" to "PL",
            "Portuguese" to "PT",
            "Romanian" to "RO",
            "Russian" to "RU",
            "Slovak" to "SK",
            "Slovenian" to "SL",
            "Swedish" to "SV",
            "Turkish" to "TR",
            "Ukrainian" to "UK",
            "Chinese" to "ZH"
        )

        fun isSupported(language: String) = supportedLanguage.containsKey(language)

        fun getSupportedLanguage() = supportedLanguage.keys.toList()
    }

    override fun asJson(): Either<Throwable, JSONObject> {
        val map = JSONObject()
        sourceLanguage.tap {
            if (!isSupported(it)) {
                return IllegalArgumentException("Source language $it is not supported.").left()
            }
            map["source_lang"] = supportedLanguage[it]
        }
        if (!isSupported(targetLanguage)) {
            return IllegalArgumentException("Target language $targetLanguage is not supported.").left()
        }
        map["target_lang"] = supportedLanguage[targetLanguage]
        map.putArray("text", text)
        return map.right()
    }

}