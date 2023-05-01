package skywolf46.devain.commands.discord.devain

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.koin.core.component.inject
import skywolf46.devain.DevAin
import skywolf46.devain.config.BotConfig
import skywolf46.devain.platform.discord.DiscordCommand
import skywolf46.devain.util.TimeUtil
import java.awt.Color

class DevAinStatusCommand : DiscordCommand() {

    private val devAin by inject<DevAin>()
    private val config by inject<BotConfig>()
    override fun createCommandInfo(): Pair<String, CommandData> {
        return "status" to
                Commands.slash("status", "봇 상태를 확인합니다.")
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.defer { _, hook ->
            hook.sendMessageEmbeds(
                EmbedBuilder()
                    .setColor(Color.GREEN)
                    .setTitle("DevAin Standalone")
                    .addField("업타임", TimeUtil.toTimeString(System.currentTimeMillis() - devAin.startTime), false)
                    .addField("서버 유저당 최대 허용 프리셋", "${config.maxUserPresetPerServer}개", true)
                    .addField("최대 허용 서버 프리셋", "${config.maxServerPreset}개", true)
                    .setFooter(devAin.version)
                    .build()
            ).queue()
        }
    }

}