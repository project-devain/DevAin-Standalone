package skywolf46.devain.model.api.cohere

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.annotations.CommandParameter
import skywolf46.devain.annotations.Required
import skywolf46.devain.apicall.networking.Request
import skywolf46.devain.util.putNotNull
import skywolf46.devain.util.putNotNullOrFatal

/**
 * Cohere Generate API Request.
 * @see [https://docs.cohere.com/reference/generate]
 */
data class CohereGenerationRequest(
    @Required @CommandParameter("prompt", "{model}에 질의할 내용입니다.") val prompt: String,
    val model: String = "command",
    @CommandParameter("max-token", "모델이 응답으로 제공할 수 있는 최대 토큰 수를 제한합니다. (기본 2048, 최대 4096)") val maxTokens: Int? = null,
    @CommandParameter(
        "temperature", "낮을수록 예측 가능한 값을, 높을수록 예측 불가능한 결과를 제공합니다. (기본 0.75, 0.0 ~ 2.0)"
    ) val temperature: Double? = null,
    @CommandParameter(
        "frequency-penalty", "해당 값이 높을수록, 이전에 반복된 토큰에 대하여 횟수에 비례해 더 강력한 패널티를 가합니다. (기본 0.0, 0.0 ~ 1.0)"
    ) val frequencyPenalty: Double? = null,
    @CommandParameter(
        "presence-penalty", "해당 값이 높을수록, 이전에 반복된 값에 대해 동일한 패널티를 가합니다. (기본 0.0, 0.0 ~ 1.0)"
    ) val presencePenalty: Double? = null,
    @CommandParameter(
        "k", "top_k 값입니다. 각 단계에서 생성할 가능성이 높은 상위 k개의 값만 고려되도록 합니다. (기본 0, 0 ~ 500)"
    ) val top_k: Int? = null,
    @CommandParameter(
        "p", "각 단계에서 총 확률 질량이 p인 가장 가능성이 높은 토큰만 고려되도록 합니다. (기본 0.0, 0.0 ~ 0.99)"
    ) val top_p: Double? = null,
    val totalGeneration: Int? = null,
    val stream: Boolean? = null,
    val truncate: TruncateType? = null,
    val likeliHoods: LikeliHoods? = null,
    val endSequence: List<String>? = null,
    val stopSequence: List<String>? = null,
) : Request<JSONObject> {

    override fun serialize(): Either<Throwable, JSONObject> {
        val json = JSONObject()
        json["prompt"] = prompt
        json["model"] = model
        json["stream"] = stream
        json.putNotNull("end_sequence", endSequence)
        json.putNotNull("stop_sequence", stopSequence)
        json.putNotNull("truncate", truncate)
        json.putNotNullOrFatal("num_generations", totalGeneration, 1..5) {
            return IllegalArgumentException("Total generation must be in range of ${it.start}..${it.endInclusive}").left()
        }
        json.putNotNullOrFatal(
            "max_tokens", maxTokens, (if (likeliHoods == LikeliHoods.ALL) 0 else 1)..Integer.MAX_VALUE
        ) {
            return IllegalArgumentException("Max tokens must be in range of ${it.start}..${it.endInclusive}").left()
        }
        json.putNotNullOrFatal("temperature", temperature, 0.0..5.0) {
            return IllegalArgumentException("Temperature must be in range of ${it.start}..${it.endInclusive}").left()
        }
        json.putNotNullOrFatal("k", top_k, 0..500) {
            return IllegalArgumentException("Top k must be in range of ${it.start}..${it.endInclusive}").left()
        }
        json.putNotNullOrFatal("p", top_p, 0.0..0.99) {
            return IllegalArgumentException("Top p must be in range of ${it.start}..${it.endInclusive}").left()
        }
        json.putNotNullOrFatal("frequency_penalty", frequencyPenalty, 0.0..1.0) {
            return IllegalArgumentException("Frequency penalty must be in range of ${it.start}..${it.endInclusive}").left()
        }
        json.putNotNullOrFatal("presence_penalty", presencePenalty, 0.0..1.0) {
            return IllegalArgumentException("Presence penalty must be in range of ${it.start}..${it.endInclusive}").left()
        }
        json.putNotNull("return_likelihoods", likeliHoods?.name)
        return json.right()
    }

    enum class TruncateType {
        START, END, NONE
    }

    enum class LikeliHoods {
        NONE, ALL, GENERATION
    }
}