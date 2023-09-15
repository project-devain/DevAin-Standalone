package skywolf46.devain.model.rest.gpt.completion.request

import arrow.core.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import skywolf46.devain.model.Request
import skywolf46.devain.model.rest.gpt.completion.OpenAIGPTMessage
import skywolf46.devain.util.checkRangeAndFatal

data class OpenAIGPTRequest(
    val modelName: String,
    val messages: List<OpenAIGPTMessage>,
    val generateAmount: Int = 1,
    val temperature: Option<Double> = None,
    val top_p: Option<Double> = None,
    val maxTokens: Option<Int> = None,
    val presencePenalty: Option<Double> = None,
    val frequencyPenalty: Option<Double> = None,
    val hidePrompt: Boolean = false,
    val createdOn: Long = System.currentTimeMillis()
) : Request<JSONObject> {
    override fun asJson(): Either<Throwable, JSONObject> {
        val map = JSONObject()
        temperature.tap {
            it.checkRangeAndFatal(0.0..2.0) { range ->
                return IllegalArgumentException("Temperature 값은 ${range.start} ~ ${range.endInclusive} 사이여야만 합니다.").left()
            }
            map["temperature"] = it
        }
        top_p.tap {
            it.checkRangeAndFatal(0.0..1.0) { range ->
                return IllegalArgumentException("top_p 값은 ${range.start} ~ ${range.endInclusive} 사이여야만 합니다.").left()
            }
            map["top_p"] = it
        }
        presencePenalty.tap {
            it.checkRangeAndFatal(-2.0..2.0) { range ->
                return IllegalArgumentException("Presence Penalty 값은 ${range.start} ~ ${range.endInclusive} 사이여야만 합니다.").left()
            }
            map["presence_penalty"] = it
        }
        frequencyPenalty.tap {
            it.checkRangeAndFatal(-2.0..2.0) { range ->
                return IllegalArgumentException("Frequency Penalty 값은 ${range.start} ~ ${range.endInclusive} 사이여야만 합니다.").left()
            }
            map["frequency_penalty"] = it
        }

        map["model"] = modelName
        map["messages"] = JSONArray().apply {
            addAll(messages.map { it.asJson().getOrNull()!! })
        }
        map["n"] = generateAmount
        return map.right()
    }
}