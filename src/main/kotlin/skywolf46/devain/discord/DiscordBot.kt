package skywolf46.devain.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import skywolf46.devain.DevAin
import skywolf46.devain.config.BotConfig
import kotlin.system.exitProcess

class DiscordBot(devAin: DevAin, config: BotConfig) {
    lateinit var jda: JDA

    private val commandAdapter by lazy {
        DiscordCommandAdapter(jda)
    }

    init {
        kotlin.runCatching {
            jda = JDABuilder.create(config.botToken, GatewayIntent.values().toList()).addEventListeners()
                .setStatus(OnlineStatus.IDLE).setActivity(Activity.listening("안")).build()
        }.onFailure {
            println("초기화 실패; 봇 초기화 중 오류가 발생하였습니다.")
            it.printStackTrace()
            exitProcess(-1)
        }
        jda.addEventListener(commandAdapter)
    }

    fun registerCommands(vararg command: DiscordCommand) : DiscordBot {
        commandAdapter.registerCommands(*command)
        return this
    }
}