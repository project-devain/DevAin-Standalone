package skywolf46.devain.model.rest.openai.completion.request

import arrow.core.*
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import skywolf46.devain.model.Request
import skywolf46.devain.model.rest.openai.completion.OpenAIGPTMessage
import skywolf46.devain.util.checkRangeAndFatal

data class OpenAIGPTRequest(
    /**
     * OpenAI Chat Completion model name.
     */
    val modelName: String,
    /**
     * Messages to send.
     * If last message is not USER, error will be thrown.
     */
    val messages: List<OpenAIGPTMessage>,
    /**
     * Amount of message to generate.
     */
    val generateAmount: Int = 1,
    /**
     * Temperature of text generation.
     * Default value is 1.0.
     *
     * If temperature is high, text will be more random.
     * If temperature is low, text will be more predictable.
     *
     * Range: 0.0 ~ 2.0
     */
    val temperature: Option<Double> = None,
    /**
     * top_p value of text generation.
     * Default value is 1.0.
     *
     * If top_p is high, text will be more random.
     * If top_p is low, text will be more predictable.
     *
     * Range: 0.0 ~ 1.0
     */
    val top_p: Option<Double> = None,
    /**
     * Max token count of text generation.
     * Default value is infinite(None).
     *
     * Range: 1 ~ Infinite
     */
    val maxTokens: Option<Int> = None,
    /**
     * Presence penalty of text generation.
     * Default value is 0.0.
     *
     * Positive values penalize new tokens based on whether they appear in the text so far,
     *  increasing the model's likelihood to talk about new topics.
     *
     * Range: -2.0 ~ 2.0
     */
    val presencePenalty: Option<Double> = None,
    /**
     * Frequency penalty of text generation.
     * Default value is 0.0.
     *
     * Positive values penalize new tokens based on their existing frequency in the text so far,
     *  decreasing the model's likelihood to repeat the same line verbatim.
     *
     * Range: -2.0 ~ 2.0
     */
    val frequencyPenalty: Option<Double> = None,
    /**
     * Whether to hide prompt or not.
     * Default value is false.
     */
    val hidePrompt: Boolean = false,
    /**
     * Functions that GPT can use.
     */
    val functions: Option<List<OpenAIFunctionDeclaration>> = None,
    /**
     * Timestamp of request.
     */
    val createdOn: Long = System.currentTimeMillis()
) : Request<JSONObject> {

    /**
     * Struct JSONObject from this request.
     */
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
        maxTokens.tap {
            it.checkRangeAndFatal(1..Int.MAX_VALUE) { range ->
                return IllegalArgumentException("최대 토큰 값은 ${range.start} ~ ${range.endInclusive} 사이여야만 합니다.").left()
            }
            map["max_tokens"] = it
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
        if (messages.isEmpty()) {
            return IllegalArgumentException("최소 1개 이상의 메시지 내역을 포함해야 합니다.").left()
        }
        if (messages.last().role != OpenAIGPTMessage.Role.USER) {
            return IllegalArgumentException("마지막 메시지는 USER 역할이어야 합니다.").left()
        }
        map["messages"] = JSONArray().apply {
            addAll(messages.map { it.asJson().getOrNull()!! })
        }
        map["n"] = generateAmount
        return map.right()
    }
}