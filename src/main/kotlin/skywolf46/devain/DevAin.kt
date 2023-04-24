package skywolf46.devain

import skywolf46.devain.commands.discord.gpt.SimpleGPTCommand
import skywolf46.devain.commands.discord.presets.user.UserPresetAddCommand
import skywolf46.devain.commands.discord.presets.user.UserPresetListCommand
import skywolf46.devain.commands.discord.presets.user.UserPresetShareCommand
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.storage.PresetStorage
import skywolf46.devain.discord.DiscordBot
import java.io.File


fun main(args: Array<String>) {
    DevAin().init()
}

class DevAin {
    val version = "1.1.0 - Double Barrel Rollcake"

    lateinit var config: BotConfig
        private set

    lateinit var discordBot: DiscordBot
        private set

    private val presetStorage = PresetStorage()

    val startTime = System.currentTimeMillis()


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
        discordBot = DiscordBot(this, config)
        initializeCommands()
    }

    private fun initializeCommands() {
        println("명령어 초기화중..")
        discordBot.registerCommands(
            SimpleGPTCommand(
                presetStorage,
                config,
                "ask",
                "GPT-3.5에게 질문합니다. GPT-4보다 비교적 빠릅니다. 세션을 보관하지 않으며, 명령어당 하나의 세션으로 인식합니다.",
                "gpt-3.5-turbo"
            ),
            SimpleGPTCommand(
                presetStorage,
                config,
                "ask-more",
                "GPT-4에게 질문합니다. GPT-4는 느리지만, 조금 더 논리적인 답변을 기대할 수 있습니다.",
                "gpt-4"),

            // User Presets
            UserPresetListCommand(presetStorage),
            UserPresetAddCommand(presetStorage),
            UserPresetShareCommand(presetStorage)
        )
    }
}