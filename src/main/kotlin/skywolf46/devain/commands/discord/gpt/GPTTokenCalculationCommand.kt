package skywolf46.devain.commands.discord.gpt

import arrow.core.right
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.ModelType
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import skywolf46.devain.platform.discord.DiscordCommand
import java.awt.Color
import java.text.DecimalFormat

class GPTTokenCalculationCommand : DiscordCommand() {
    companion object {

        private val priceInfo = mapOf("gpt-4" to 0.03, "gpt-3.5-turbo" to 0.002)
        private const val dollarToWonMultiplier = 1322.50
        private val decimalFormat = DecimalFormat("#,###")
    }

    override fun createCommandInfo(): Pair<String, CommandData> {
        return "tokens" to
                Commands.slash("tokens", "GPT 모델이 이 토큰을 몇 토큰으로 인식할지 확인합니다.")
                    .addOption(OptionType.STRING, "prompt", "대상 프롬프트입니다.", true)
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferEmbed { _, hook ->
            val prompt = event.getOption("prompt")!!.asString
            val encoded = Encodings.newDefaultEncodingRegistry().getEncodingForModel(ModelType.GPT_4_32K).encode(prompt)
            EmbedBuilder()
                .setTitle("분석 완료")
                .setColor(Color.MAGENTA)
                .addField("모델 기준", "GPT-4 (CL_100K_BASE)", false)
                .addField("원본 메시지", "${prompt.length}자", true)
                .addField("토큰", "${encoded.size}개", true)
                .addField(
                    "예상 비용",
                    "${decimalFormat.format(priceInfo["gpt-4"]!! * encoded.size.toDouble() / 1000.0 * dollarToWonMultiplier)}원",
                    true
                )
                .build().right()
        }
    }


}