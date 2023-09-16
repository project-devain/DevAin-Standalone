package skywolf46.devain.model.rest.openai.dalle

import arrow.core.*
import org.json.simple.JSONObject
import skywolf46.devain.model.Request
import skywolf46.devain.util.checkRangeAndFatal

data class DallERequest(
    val prompt: String,
    val imageSize: ImageSize = ImageSize.X256,
    val responseType: ResponseType = ResponseType.URL,
    val generateCount: Int,
    val createdOn: Long = System.currentTimeMillis()
) : Request<JSONObject> {

    override fun asJson(): Either<Throwable, JSONObject> {
        if (prompt.length > 1000) {
            return IllegalArgumentException("프롬프트는 1000자를 넘을 수 없습니다.").left()
        }
        generateCount.checkRangeAndFatal(1..10) {
            return IllegalArgumentException("생성 개수는 1개 이상 10개 이하로 설정해야 합니다.").left()
        }
        return JSONObject().apply {
            this["prompt"] = prompt
            this["n"] = generateCount
            this["size"] = imageSize.requestType
            this["response_format"] = responseType.name.lowercase()
        }.right()
    }


    enum class ImageSize(val requestType: String) {
        X256("256x256"), X512("%512x512"), X1024("1024x1024")
    }

    enum class ResponseType {
        URL, B64_JSON
    }
}