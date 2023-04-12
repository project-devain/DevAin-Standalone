package skywolf46.devain

import skywolf46.devain.config.BotConfig
import skywolf46.devain.discord.DiscordBot
import java.io.File


fun main(args: Array<String>) {
    DevAin().init()
}

class DevAin {
    val version = "1.0.1 - Dead End GingerBread"

    lateinit var config: BotConfig
        private set

    lateinit var discordBot: DiscordBot
        private set


    internal fun init() {
        println("DevAin $version - 초기화 시작")
        loadConfig()
        initializeBot()
    }

    private fun loadConfig() {
        println("설정 불러오는 중..")
        config = BotConfig(File("settings.properties"))
    }

    private fun initializeBot() {
        println("디스코드 봇 활성화중..")
        discordBot = DiscordBot(config)
    }
}