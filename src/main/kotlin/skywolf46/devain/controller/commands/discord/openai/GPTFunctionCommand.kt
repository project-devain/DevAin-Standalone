package skywolf46.devain.controller.commands.discord.openai

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.koin.core.component.get
import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceUserDataCall
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey
import skywolf46.devain.model.store.OpenAIFunctionStore
import skywolf46.devain.platform.discord.ImprovedDiscordCommand

class GPTFunctionCommand : ImprovedDiscordCommand("gpt-function", "GPT 펑션 사용 여부를 변경합니다.") {
    val builtInChoices = listOf(
        "weather",
        "exchange-rate"
    )

    private val store = get<OpenAIFunctionStore>()

    private val apiCall by inject<DevAinPersistenceUserDataCall>()

    override fun modifyCommandData(options: SlashCommandData) {
        options
            .addOption(OptionType.STRING, "function", "대상 펑션 이름을 지정합니다. 빌트인이 아닌 경우, '유저/펑션명'으로 지정합니다.", true, true)
            .addOption(OptionType.BOOLEAN, "enable", "펑션을 활성화할지 여부를 지정합니다.", true)
    }

    override suspend fun onCommand(event: SlashCommandInteractionEvent) {
        val function = event.getOption("function")!!.asString

        if (function.contains("/")) {
            val user = function.substringBefore('/')
            val func = function.substringAfter('/')
            if (store.getFunction(OpenAIFunctionKey(function)).isEmpty()) {
                event.reply("해당 유저의 펑션을 찾을 수 없습니다.").queue()
                return
            }
            if (event.getOption("enable")!!.asBoolean) {
                event.reply("펑션 ${function}을 활성화했습니다.").queue()
            } else {
                event.reply("펑션 ${function}을 비활성화했습니다.").queue()
            }
        } else {
            if (function !in builtInChoices) {
                event.reply("해당 펑션을 찾을 수 없습니다.").queue()
                return
            }
            if (event.getOption("enable")!!.asBoolean) {
                event.reply("펑션 ${function}을 활성화했습니다.").queue()
            } else {
                event.reply("펑션 ${function}을 비활성화했습니다.").queue()
            }
        }
    }

    override suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        if (event.focusedOption.name == "function") {
            val currentValue = event.focusedOption.value
//            if (currentValue.contains('/')) {
//                val user = currentValue.substringBefore('/')
//                val func = currentValue.substringAfter('/')
//                if (userChoices.containsKey(user)) {
//                    event.replyChoiceStrings(
//                        userChoices[user]!!.filter { it.startsWith(func) }.map { "$user/$it" }
//                    ).queue()
//                } else {
//                    event.replyChoiceStrings(emptyList()).queue()
//                }
//            } else {
//                event.replyChoiceStrings(
//                    builtInChoices.filter { it.startsWith(currentValue) }
//                ).queue()
//            }
        }
    }
}