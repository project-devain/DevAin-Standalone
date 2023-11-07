package skywolf46.devain.model.api.cohere

import arrow.core.*
import org.json.simple.JSONObject
import skywolf46.devain.model.Request
import skywolf46.devain.util.checkRangeAndFatal

/**
 * Cohere Generate API Request.
 * @see [https://docs.cohere.com/reference/generate]
 */
data class CohereGenerationRequest(
    val prompt: String,
    val model: String,
    val totalGeneration: Int,
    val stream: Boolean,
    val truncate: Option<TruncateType> = None,
    val maxTokens: Option<Int> = None,
    val temperature: Option<Double> = None,
    val top_k: Option<Int> = None,
    val top_p: Option<Double> = None,
    val frequencyPenalty: Option<Double> = None,
    val presencePenalty: Option<Double> = None,
    val likeliHoods: Option<LikeliHoods> = None,
    val endSequence: Option<Array<String>> = None,
    val stopSequence: Option<Array<String>> = None,
) : Request<JSONObject> {

    override fun asJson(): Either<Throwable, JSONObject> {
        val json = JSONObject()
        json["prompt"] = prompt
        json["model"] = model
        json["num_generations"] = totalGeneration.checkRangeAndFatal(1..5) {
            return IllegalArgumentException("Total generation must be in range of ${it.first}..${it.last}").left()
        }
        json["stream"] = stream
        maxTokens.tap {
            json["max_tokens"] =
                it.checkRangeAndFatal((if (likeliHoods.getOrElse { null } == LikeliHoods.ALL) 0 else 1)..Integer.MAX_VALUE) {
                    return IllegalArgumentException("Max tokens must be in range of ${it.first}..${it.last}").left()
                }
        }

        truncate.tap {
            json["truncate"] = it.name
        }
        temperature.tap {
            json["temperature"] = it.checkRangeAndFatal(0.0..5.0) {
                return IllegalArgumentException("Max tokens must be in range of ${it.start}..${it.endInclusive}").left()
            }
        }
        endSequence.tap {
            json["end_sequence"] = it
        }
        stopSequence.tap {
            json["stop_sequence"] = it
        }
        top_k.tap {
            json["k"] = it.checkRangeAndFatal(0..500) {
                return IllegalArgumentException("Top k must be in range of ${it.start}..${it.endInclusive}").left()
            }
        }
        top_p.tap {
            json["p"] = it.checkRangeAndFatal(0.0..0.99) {
                return IllegalArgumentException("Top p must be in range of ${it.start}..${it.endInclusive}").left()
            }
        }
        frequencyPenalty.tap {
            json["frequency_penalty"] = it.checkRangeAndFatal(0.0..1.0) {
                return IllegalArgumentException("Frequency penalty must be in range of ${it.start}..${it.endInclusive}").left()
            }
        }
        presencePenalty.tap {
            json["presence_penalty"] = it.checkRangeAndFatal(0.0..1.0) {
                return IllegalArgumentException("Presence penalty must be in range of ${it.start}..${it.endInclusive}").left()
            }
        }
        likeliHoods.tap {
            json["return_likelihoods"] = it.name
        }
        return json.right()
    }

    enum class TruncateType {
        START, END, NONE
    }

    enum class LikeliHoods {
        NONE, ALL, GENERATION
    }
}