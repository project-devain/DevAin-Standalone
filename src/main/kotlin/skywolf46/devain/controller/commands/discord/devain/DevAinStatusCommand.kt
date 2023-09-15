package skywolf46.devain.controller.commands.discord.devain

import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.koin.core.component.get
import skywolf46.devain.*
import skywolf46.devain.controller.api.requests.devain.DevAinAppPropertiesAPICall
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.model.rest.devain.data.request.GetRequest
import skywolf46.devain.platform.discord.ImprovedDiscordCommand
import skywolf46.devain.util.TimeUtil
import java.awt.Color

class DevAinStatusCommand : ImprovedDiscordCommand("status", "DevAin 봇의 상태를 확인합니다.") {
    private val startedOn = System.currentTimeMillis()
    private val propertiesApiCall = get<DevAinAppPropertiesAPICall>()
    private val countApiCall = get<DevAinPersistenceCountAPICall>()

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferEmbed { _, hook ->
            EmbedBuilder()
//                .setTitle("DevAin Standalone (${propertiesApiCall.certainly()["version"]})")
                .setTitle("DevAin Standalone (1.2.0-Grenade-Muffin)")
                .setColor(Color.CYAN)
                .addField(
                    "업타임",
                    box(TimeUtil.toTimeString((System.currentTimeMillis() - startedOn))),
                    false
                )
                .addField("", "**OpenAI GPT**", false)
                .addField(
                    "처리 완료 개수",
                    box("%,d개".format(countApiCall.call(GetRequest(KEY_GPT_PROCEED_COUNT)).getOrElse {
                        return@deferEmbed it.getErrorMessage().left()
                    }.value)),
                    true
                )
                .addField(
                    "처리 완료 토큰",
                    box("%,d토큰".format(countApiCall.call(GetRequest(KEY_GPT_PROCEED_TOKEN)).getOrElse {
                        return@deferEmbed it.getErrorMessage().left()
                    }.value)),
                    true
                )
                .addField(
                    "요청 처리 시간",
                    box(TimeUtil.toTimeString(countApiCall.call(GetRequest(KEY_GPT_PROCEED_TIME)).getOrElse {
                        return@deferEmbed it.getErrorMessage().left()
                    }.value)),
                    true
                )

                .addField("", "**DeepL Translation**", false)
                .addField(
                    "총 번역 횟수",
                    box("%,d개".format(countApiCall.call(GetRequest(KEY_DEEPL_PROCEED_COUNT)).getOrElse {
                        return@deferEmbed it.getErrorMessage().left()
                    }.value)),
                    true
                )
                .addField(
                    "번역 완료 문자열",
                    box("%,d자".format(countApiCall.call(GetRequest(KEY_DEEPL_PROCEED_TOKEN)).getOrElse {
                        return@deferEmbed it.getErrorMessage().left()
                    }.value)),
                    true
                )
                .build().right()
        }
    }
}