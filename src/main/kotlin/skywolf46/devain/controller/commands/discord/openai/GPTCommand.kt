package skywolf46.devain.controller.commands.discord.openai

import arrow.core.toOption
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import skywolf46.devain.model.api.openai.completion.OpenAIGPTMessage
import skywolf46.devain.model.api.openai.completion.OpenAIGPTRequest
import skywolf46.devain.model.api.openai.completion.OpenAIGPTResponse
import skywolf46.devain.platform.discord.ImprovedDiscordCommand
import skywolf46.devain.util.TimeUtil
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.round

abstract class GPTCommand(command: String, description: String) :
    ImprovedDiscordCommand(command, description, command.toOption()) {

    companion object {
        const val DEFAULT_MODEL = "gpt-4"
        private val priceInfo =
            mapOf("gpt-4-0613" to 0.06, "gpt-4" to 0.06, "gpt-3.5-turbo" to 0.002, "gpt-3.5-turbo-16k" to 0.002)
        private const val dollarToWonMultiplier = 1329.41
        private val decimalFormat = DecimalFormat("#,###")
    }

    fun isEmbedCompatible(request: OpenAIGPTRequest, response: OpenAIGPTResponse): Boolean {
        val requestMessage = request.messages.find { it.role == OpenAIGPTMessage.Role.USER }!!
        val responseMessage = response.answers.last()
        return (request.hidePrompt || requestMessage.content.find { it.first == "text" }?.second.toString().length < 4096) && (responseMessage.message.content.find { it.first == "text" }?.second.toString().length < 1024 - 6)
    }

    fun buildEmbedded(request: OpenAIGPTRequest, response: OpenAIGPTResponse): MessageEmbed {
        val requestMessage = request.messages.find { it.role == OpenAIGPTMessage.Role.USER }!!
        val responseMessage = response.answers.last()
        return EmbedBuilder()
            .apply {
                setTitle("Request complete - ${request.modelName}")
                setColor(Color(162, 103, 181))
                if (!request.hidePrompt) {
                    setDescription(requestMessage.content.find { it.first == "text" }?.second.toString())
                } else {
                    setDescription("_프롬프트 숨겨짐_")
                }

                addField(
                    "Response",
                    responseMessage.message.content.find { it.first == "text" }?.second.toString(),
                    false
                )
                addField(
                    "Tokens",
                    box(
                        "%,d+%,d=%,d".format(
                            response.usage.promptToken,
                            response.usage.completionToken,
                            response.usage.totalToken
                        )
                    ),
                    true
                )
                if (request.modelName in priceInfo) {
                    val token = response.usage.totalToken.toDouble() / 1000.0
                    val price = round(priceInfo[request.modelName]!! * token * 10000.0) / 10000.0
                    val won = round(dollarToWonMultiplier * price * 1000) / 1000.0
                    addField(
                        "Price", box("$%.3f (%.1f원)".format(price, won)), true
                    )
                } else {
                    addField(
                        "Price", box("알 수 없음"), true
                    )
                }

                addField("Process", box(TimeUtil.toTimeString(System.currentTimeMillis() - request.createdOn)), true)

                request.maxTokens.onSome {
                    addField("Max Token", box(it.toString()), true)
                }
                request.top_p.onSome {
                    addField("top_p", box(it.toString()), true)
                }
                request.temperature.onSome {
                    addField("Temperture", box(it.toString()), true)
                }
                request.frequencyPenalty.onSome {
                    addField("Frequency Penalty", box(it.toString()), true)
                }
                request.presencePenalty.onSome {
                    addField("Presence Penalty", box(it.toString()), true)
                }
                if (request.showFunctionTrace) {
                    addField("Function Trace", response.stackTrace.buildStackTrace(), false)
                }
            }
            .build()
    }
}