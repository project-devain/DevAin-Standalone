package skywolf46.devain.discord

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import skywolf46.devain.config.BotConfig
import skywolf46.devain.listener.EventListener
import kotlin.system.exitProcess

class DiscordBot(config: BotConfig) {
    lateinit var jda: JDA

    init {
        kotlin.runCatching {
            jda = JDABuilder.create(config.botToken, GatewayIntent.values().toList()).addEventListeners()
                .setStatus(OnlineStatus.IDLE)
//                .setActivity(Activity.listening("rm -rf /"))
                .setActivity(Activity.listening("안")).build()
        }.onFailure {
            println("초기화 실패; 봇 초기화 중 오류가 발생하였습니다.")
            it.printStackTrace()
            exitProcess(-1)
        }
        jda.addEventListener(EventListener(config))
        jda.updateCommands().addCommands(
            Commands.slash("ask", "ChatGPT-3.5에게 질문합니다. GPT-4보다 비교적 빠릅니다. 세션을 보관하지 않으며, 명령어당 하나의 세션으로 인식합니다.")
                .addOption(OptionType.STRING, "contents", "ChatGPT-3.5에게 질문할 내용입니다.", true).addOption(
                    OptionType.NUMBER,
                    "temperature",
                    "모델의 temperature 값을 설정합니다. 값이 낮을수록 결정적이 되며, 높을수록 더 많은 무작위성이 가해집니다. (기본 1, 0-1.5내의 소수)",
                    false
                ).addOption(
                    OptionType.NUMBER,
                    "top_p",
                    "모델의 top_p 값을 설정합니다. 값이 낮을수록 토큰의 샘플링 범위가 낮아집니다. (기본 1, 0-1.5내의 소수)",
                    false
                ).addOption(OptionType.INTEGER, "max-token", "최대 토큰 개수를 설정합니다.")
                .addOption(OptionType.BOOLEAN, "hide-prompt", "결과 창에서 프롬프트를 숨깁니다. 명령어 클릭은 숨겨지지 않습니다."),
            Commands.slash("ask-more", "ChatGPT-4에게 질문합니다. 세션을 보관하지 않으며, 명령어당 하나의 세션으로 인식합니다.")
                .addOption(OptionType.STRING, "contents", "ChatGPT-3.5에게 질문할 내용입니다.", true).addOption(
                    OptionType.NUMBER,
                    "temperature",
                    "모델의 temperature 값을 설정합니다. 값이 낮을수록 결정적이 되며, 높을수록 더 많은 무작위성이 가해집니다. (기본 1, 0-1.5내의 소수)",
                    false
                ).addOption(
                    OptionType.NUMBER,
                    "top_p",
                    "모델의 top_p 값을 설정합니다. 값이 낮을수록 토큰의 샘플링 범위가 낮아집니다. (기본 1, 0-1.5내의 소수)",
                    false
                ).addOption(OptionType.INTEGER, "max-token", "최대 토큰 개수를 설정합니다.")
                .addOption(OptionType.BOOLEAN, "hide-prompt", "결과 창에서 프롬프트를 숨깁니다. 명령어 클릭은 숨겨지지 않습니다."),
            Commands.slash("askto", "OpenAI의 특정 모델에게 질문합니다. 세션을 보관하지 않으며, 명령어당 하나의 세션으로 인식합니다.")
                .addOption(OptionType.STRING, "model", "OpenAI의 모델 중 질문할 모델입니다.", true, true)
                .addOption(OptionType.STRING, "contents", "OpenAI의 모델 중 선택할 모델입니다.", true).addOption(
                    OptionType.NUMBER,
                    "temperature",
                    "모델의 temperature 값을 설정합니다. 값이 낮을수록 결정적이 되며, 높을수록 더 많은 무작위성이 가해집니다. (기본 1, 0-1.5내의 소수)",
                    false
                ).addOption(
                    OptionType.NUMBER,
                    "top_p",
                    "모델의 top_p 값을 설정합니다. 값이 낮을수록 토큰의 샘플링 범위가 낮아집니다. (기본 1, 0-1.5내의 소수)",
                    false
                ).addOption(OptionType.INTEGER, "max-token", "최대 토큰 개수를 설정합니다.")
                .addOption(OptionType.BOOLEAN, "hide-prompt", "결과 창에서 프롬프트를 숨깁니다. 명령어 클릭은 숨겨지지 않습니다."),
            Commands.slash("edit", "OpenAI의 API를 이용해 텍스트를 다듬습니다.")
                .addOption(OptionType.STRING, "instruction", "AI가 텍스트를 어떻게 다듬을지 선택지를 입력합니다.", true)
                .addOption(OptionType.STRING, "input", "AI가 수정할 원본 텍스트입니다.", true),
            Commands.slash("imagine", "Dall-E를 사용해 프롬프트 기반으로 이미지를 생성합니다.")
                .addOption(OptionType.STRING, "prompt", "이미지를 설명할 프롬프트입니다.", true),
            Commands.slash("predicate", "GPT 모델의 비용을 예상합니다.")
                .addOption(OptionType.STRING, "model", "대상 모델입니다.", true, true)
                .addOption(OptionType.INTEGER, "input", "입력 토큰 개수입니다.", true)
                .addOption(OptionType.INTEGER, "output", "출력 토큰 개수입니다.", true),
            Commands.slash("dream", "DreamStudio를 이용하여 이미지를 생성합니다.")
                .addOption(OptionType.STRING, "engine", "사용할 모델입니다.", true, true)
                .addOption(OptionType.STRING, "prompt", "텍스트 프롬프트입니다.", true)
                .addOption(OptionType.INTEGER, "step", "사용할 step입니다. 기본은 40으로 설정됩니다.")
                .addOption(OptionType.NUMBER, "cfg_scale", "사용할 cfg scale입니다. 기본은 7로 설정됩니다.")
        ).queue()
    }
}