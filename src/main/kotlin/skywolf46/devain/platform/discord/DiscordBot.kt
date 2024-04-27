package skywolf46.devain.platform.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import kotlin.system.exitProcess

class DiscordBot {
    lateinit var jda: JDA

    private val commandAdapter by lazy {
        DiscordCommandAdapter(jda)
    }

    private val commands = mutableListOf<DiscordCommand>()


    fun registerCommands(vararg command: DiscordCommand): DiscordBot {
        commands.addAll(command)
        return this
    }

    fun registerCommands(vararg command: ImprovedDiscordCommand): DiscordBot {
        commands.addAll(command)
        return this
    }

    internal fun finishSetup(apiToken: String) {
        kotlin.runCatching {
            jda = JDABuilder.create(apiToken, GatewayIntent.values().toList()).addEventListeners()
                .setStatus(OnlineStatus.IDLE).setActivity(Activity.listening("안")).build().awaitReady()
        }.onFailure {
            println("초기화 실패; 봇 초기화 중 오류가 발생하였습니다.")
            it.printStackTrace()
            exitProcess(-1)
        }
        jda.addEventListener(commandAdapter)
        commandAdapter.registerCommands(*commands.toTypedArray())
    }
}