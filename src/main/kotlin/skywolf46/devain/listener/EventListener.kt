package skywolf46.devain.listener

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import skywolf46.devain.config.BotConfig

class EventListener(private val config: BotConfig) : ListenerAdapter() {
    val models = mutableListOf<String>(
        "gpt-4",
        "gpt-4-0314",
        "gpt-4-32k",
        "gpt-4-32k-0314",
        "gpt-3.5-turbo",
        "gpt-3.5-turbo-0301",
    )

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "ask" -> GPTChatCompletionHandler.handle(config, "gpt-3.5-turbo", event)
            "ask-more" -> GPTChatCompletionHandler.handle(config, "gpt-4", event)
            "edit" -> OpenAIEditTextHandler.handle(config, "text-davinci-edit-001", event)
            "askto" -> {
                val model = event.getOption("model")!!.asString
                if (!models.contains(model)) {
                    event.reply("OpenAI에 등록되지 않은 모델이거나, DevAin에서 사용이 금지된 모델입니다.").queue()
                    return
                }
                GPTChatCompletionHandler.handle(config, model, event)
            }
            "imagine" -> DallEImageHandler.handle(config, event)

        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (event.name == "askto") {
            event.replyChoiceStrings(models.filter { it.startsWith(event.focusedOption.value) }).queue()
        }
    }
}