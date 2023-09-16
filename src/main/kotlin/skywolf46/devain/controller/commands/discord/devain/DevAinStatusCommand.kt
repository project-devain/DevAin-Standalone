package skywolf46.devain.controller.commands.discord.devain

import arrow.core.right
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.koin.core.component.get
import org.koin.core.component.inject
import skywolf46.devain.platform.discord.DiscordBot
import skywolf46.devain.platform.discord.ImprovedDiscordCommand
import skywolf46.devain.platform.plugin.PluginManager
import skywolf46.devain.util.TimeUtil
import java.awt.Color
import java.lang.management.ManagementFactory
import javax.management.ObjectName

class DevAinStatusCommand : ImprovedDiscordCommand("status", "DevAin 봇의 상태를 확인합니다.") {
    private val startedOn = System.currentTimeMillis()
    private val pluginManager = get<PluginManager>()
    private val bot by inject<DiscordBot>()

    override fun modifyCommandData(options: SlashCommandData) {
        options.addCompletableOption("plugin", "정보를 확인할 플러그인을 확인합니다.", false) {
            pluginManager.getPlugins().map { it.pluginName }
        }
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        if (event.getOption("plugin") == null)
            onBotStatus(event)
        else
            onPluginStatus(event, event.getOption("plugin")!!.asString)
    }

    private fun onBotStatus(event: SlashCommandInteractionEvent) {
        event.deferEmbed { _, hook ->
            val cpuLoad = ManagementFactory.getPlatformMBeanServer()
                .getAttribute(ObjectName("java.lang:type=OperatingSystem"), "CpuLoad")
            val memoryLoad =
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).toDouble() / 1024.0 / 1024.0
            EmbedBuilder()
//                .setTitle("DevAin Standalone (${propertiesApiCall.certainly()["version"]})")
                .setTitle("DevAin Standalone (1.2.0-Grenade-Muffin)")
                .setColor(Color.CYAN)
                .addField(
                    "업타임",
                    box(TimeUtil.toTimeString((System.currentTimeMillis() - startedOn))),
                    false
                )
                .addField("Host OS", box(System.getProperty("os.name")), false)
                .addField("CPU", box("%.2f%%".format(cpuLoad)), true)
                .addField("RAM", box("%.2fMB".format(memoryLoad)), true)
                .addField("등록된 서버", box("%,d개".format(bot.jda.guilds.size)), true)
                .addField(
                    "플러그인 (${pluginManager.getPlugins().size})", box(
                        pluginManager.getPlugins().joinToString("\n") {
                            if (pluginManager.isEnabled(it))
                                "${color(false, 32)}${it.pluginName} (${it.getVersion()})${reset()}"
                            else
                                "${color(true, 31)}${it.pluginName} (${it.getVersion()})${reset()}"
                        }, "ansi"
                    ), false
                )
                .build().right()
        }
    }

    private suspend fun onPluginStatus(event: SlashCommandInteractionEvent, pluginName: String) {
        val plugin = pluginManager.getPluginByName(pluginName) ?: run {
            event.reply("등록되지 않은 플러그인입니다.").queue()
            return
        }

        if (!pluginManager.isEnabled(plugin)) {
            event.reply("비활성화된 플러그인입니다.").queue()
            return
        }
        val statistics = plugin.getStatistics()
        if (statistics.isEmpty()) {
            event.reply("이 플러그인은 통계를 제공하지 않습니다.").queue()
            return
        }
        event.deferEmbed { _, hook ->
            EmbedBuilder()
//                .setTitle("DevAin Standalone (${propertiesApiCall.certainly()["version"]})")
                .apply {
                    setTitle("DevAin Standalone (1.2.0-Grenade-Muffin)")
                    setColor(Color.CYAN)
                    addField("", "**Plugin Statistics ($pluginName)**", false)
                    for ((key, value) in statistics) {
                        addField("", "**${key}**", false)
                        for (statisticsColumn in value)
                            addField(statisticsColumn.name, box(statisticsColumn.value), true)
                    }
                }
                .build().right()
        }
    }

    private fun color(bold: Boolean, colorId: Int): String {
        return "\u001B[${if (bold) 1 else 0};${colorId}m"
    }

    private fun reset(): String {
        return "\u001B[0m"
    }
}