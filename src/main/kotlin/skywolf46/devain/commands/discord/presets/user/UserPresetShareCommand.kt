package skywolf46.devain.commands.discord.presets.user

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.koin.core.component.inject
import skywolf46.devain.data.storage.PresetStorage
import skywolf46.devain.platform.discord.DiscordCommand
import java.awt.Color

class UserPresetShareCommand : DiscordCommand() {

    private val storage by inject<PresetStorage>()

    override fun createCommandInfo(): Pair<String, CommandData> {
        return "preset-share" to Commands.slash("preset-share", "사용자의 프리셋을 공유합니다.")
            .addOption(OptionType.STRING, "name", "대상 프리셋을 지정합니다.", true)
            .addOption(OptionType.BOOLEAN, "open-source", "프리셋의 공개 여부입니다.", true)
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        event.deferEmbed { _, hook ->
            val preset = storage.getPresetById(
                event.guild!!.idLong,
                event.member!!.idLong,
                event.getOption("name")!!.asString,
                false
            )
            return@deferEmbed preset.toEither { null }.mapLeft {
                "등록되지 않은 프리셋입니다."
            }.map {
                if (event.getOption("open-source")!!.asBoolean) {
                    storage.updatePreset(event.guild!!.idLong, event.member!!.idLong, it.name, it.contents, true)
                    EmbedBuilder()
                        .setTitle("프리셋 공유됨")
                        .setColor(Color.GREEN)
                        .setDescription("${event.member!!.asMention}님이 프리셋 `${it.name}`을 공개 상태로 전환하였습니다.")
                        .build()
                } else {
                    storage.updatePreset(event.guild!!.idLong, event.member!!.idLong, it.name, it.contents, false)
                    EmbedBuilder()
                        .setTitle("프리셋 비공개됨")
                        .setColor(Color.RED)
                        .setDescription("${event.member!!.asMention}님이 프리셋 `${it.name}`을 비공개로 전환하였습니다.")
                        .build()
                }
            }

        }
    }
}