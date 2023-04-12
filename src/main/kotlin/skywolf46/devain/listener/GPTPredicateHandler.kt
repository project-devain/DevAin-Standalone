package skywolf46.devain.listener

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import skywolf46.devain.config.BotConfig
import java.text.DecimalFormat

object GPTPredicateHandler {

    private val priceInfo = mapOf("gpt-4" to (0.03 to 0.06), "gpt-3.5-turbo" to (0.002 to 0.002))
    private const val dollarToWonMultiplier = 1322.50
    private val databaseTime = "2023-04-11"
    private val decimalFormat = DecimalFormat("#.###")
    private val decimalFormatInteger = DecimalFormat("#,###")
    fun handle(config: BotConfig, event: SlashCommandInteractionEvent) {
        val model = event.getOption("model")!!.asString
        val input = kotlin.runCatching {
            event.getOption("input")!!.asInt
        }.getOrElse {
            event.reply("입력 토큰 개수가 Int 최대치를 넘었습니다 : 요청이 거부됩니다.").queue()
            return
        }
        val output = kotlin.runCatching {
            event.getOption("output")!!.asInt
        }.getOrElse {
            event.reply("출력 토큰 개수가 Int 최대치를 넘었습니다 : 요청이 거부됩니다.").queue()
            return
        }
        if (input <= 0) {
            event.reply("입력 토큰 개수는 1 이상이여야만 합니다.").queue()
            return
        }
        if (output <= 0) {
            event.reply("출력 토큰 개수는 1 이상이여야만 합니다.").queue()
            return
        }
        if (!priceInfo.containsKey(model)) {
            event.reply("등록되지 않은 모델입니다.").queue()
            return
        }
        event.deferReply(false).queue { hook ->
            val predicated = priceInfo[model]!!
            val inputPrice = input.toDouble() / 1000.0 * predicated.first
            val outputPrice = output.toDouble() / 1000.0 * predicated.second
            val finalPrice = inputPrice + outputPrice
            hook.sendMessageEmbeds(
                EmbedBuilder().setTitle("비용 계산").setDescription(
                    "**토큰 개수:** ${decimalFormatInteger.format(input)} IN / ${decimalFormatInteger.format(output)} OUT\n**프롬프트 비용:** $${
                        decimalFormat.format(
                            inputPrice
                        )
                    } (${
                        decimalFormat.format(
                            inputPrice * dollarToWonMultiplier
                        )
                    }원)\n**응답 비용:** \$${decimalFormat.format(outputPrice)} (${
                        decimalFormat.format(
                            outputPrice * dollarToWonMultiplier
                        )
                    }원)\n\n그러므로, 총 비용은 $${decimalFormat.format(finalPrice)} (${decimalFormat.format(finalPrice * dollarToWonMultiplier)}원)으로 추정됩니다."
                ).setFooter("환율 데이터 기준: ${databaseTime} ($1 = ${dollarToWonMultiplier})").build()
            ).queue()
        }
    }
}