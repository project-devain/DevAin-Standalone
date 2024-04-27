package skywolf46.devain.model.api.rplus

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import org.json.simple.JSONObject
import skywolf46.devain.annotations.CommandParameter
import skywolf46.devain.annotations.Required
import skywolf46.devain.apicall.networking.Request
import skywolf46.devain.util.putNotNull

data class RPlusRequest(
    @Required
    @CommandParameter("prompt", "{model}에 질의할 내용입니다.")
    val message: String,
    val model: String = "command-r",
    @CommandParameter("temperature", "낮을수록 예측 가능한 값을, 높을수록 예측 불가능한 결과를 제공합니다. (기본 0.75, 0.0 ~ 2.0)")
    val temperature: Float? = null,
    @CommandParameter("max-token", "모델이 응답으로 제공할 수 있는 최대 토큰 수를 제한합니다. (기본 2048, 최대 4096)")
    val maxTokens: Long? = null,
    @CommandParameter("max-input-token", "모델에 최대로 입력 가능한 토큰 수를 제한합니다. (기본 2048, 최대 4096)")
    val maxInputTokens: Long? = null,
    @CommandParameter("k", "제공된 설명이 존재하지 않습니다.")
    val k: Long? = null,
    @CommandParameter("p", "제공된 설명이 존재하지 않습니다.")
    val p: Long? = null,
    @CommandParameter("seed", "프롬프트의 기초로 사용될 시드 값입니다.")
    val seed: Long? = null,
    @CommandParameter("frequency-penalty", "해당 값이 높을수록, 이전에 반복된 토큰에 대하여 횟수에 비례해 더 강력한 패널티를 가합니다. (기본 0.0, 0.0 ~ 1.0)")
    val frequencyPenalty: Float? = null,
    @CommandParameter("presence-penalty", "해당 값이 높을수록, 이전에 반복된 값에 대해 동일한 패널티를 가합니다. (기본 0.0, 0.0 ~ 1.0)")
    val presencePenalty: Float? = null,
    @CommandParameter("preamble", "AI의 역할 및 규칙을 미리 제정합니다.")
    val preamble: String? = null,
    val stream: Boolean? = null,
    val truncation: Option<PromptTruncation> = None,
    val history: Option<List<RPlusMessageHistory>> = None,
) : Request<JSONObject> {


    override fun serialize(): Either<Throwable, JSONObject> {
        val json = JSONObject()
        json["message"] = message
        json["model"] = model
        json.putNotNull("preamble", preamble)
        return json.right()
    }

    enum class PromptTruncation {
        AUTO, AUTO_PRESERVE_ORDER, OFF
    }


}