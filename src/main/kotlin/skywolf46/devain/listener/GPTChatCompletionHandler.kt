package skywolf46.devain.listener

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.FileUpload
import skywolf46.devain.config.BotConfig
import skywolf46.devain.util.OpenAiRequest
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.lang.StringBuilder
import kotlin.math.round

object GPTChatCompletionHandler {
    private val priceInfo = mapOf("gpt-4" to 0.06, "gpt-3.5-turbo" to 0.002)
    private const val dollarToWonMultiplier = 1297

    fun handle(config: BotConfig, model: String, event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue { hook ->
            processRequest(hook, event.getOption("contents")!!.asString, model, config)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun processRequest(hook: InteractionHook, message: String, model: String, config: BotConfig) {
        GlobalScope.launch {
            kotlin.runCatching {
                val result = OpenAiRequest.requestGpt(config.openAIToken, model, message)
                result.onLeft {
                    hook.sendMessage("OpenAI API가 오류를 반환하였습니다 : $it").queue()
                }.onRight {
                    val builder = StringBuilder()
                    builder
                        .append("**모델**: $model\n\n")
                        .append("**API 소모**: ${it.tokenUsage.totalTokens}토큰")
                    if (model in priceInfo) {
                        val token = it.tokenUsage.totalTokens.toDouble() / 10000.0
                        val price = round(priceInfo[model]!! * token * 10000.0) / 10000.0
                        builder.append(" ($${price}, 추산치 ${round(dollarToWonMultiplier * price * 1000) / 1000.0}원)")
                    }
                    builder.append("\n\n")
                        .append("**요청**: ${message}\n\n")
                        .append("**응답**: ${it.choices[0].answer}")
                    if (builder.length >= 2000) {
                        hook.sendFiles(FileUpload.fromData(builder.toString().toByteArray(), "answer.txt")).queue()
                    } else {
                        hook.sendMessage(builder.toString())
                            .queue()
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}