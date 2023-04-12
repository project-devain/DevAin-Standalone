package skywolf46.devain.listener

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
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
    val modelsToPrice = mutableListOf<String>(
        "gpt-4",
        "gpt-3.5-turbo",
    )
    val engines = mutableListOf<String>(
        "stable-diffusion-v1-5",
        "stable-diffusion-512-v2-1",
        "stable-diffusion-768-v2-1"
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
            "predicate" -> GPTPredicateHandler.handle(config, event)
            "dream" -> {
                val engine = event.getOption("engine")!!.asString
                if (!engines.contains(engine)) {
                    event.reply("OpenAI에 등록되지 않은 모델이거나, DevAin에서 사용이 금지된 모델입니다.").queue()
                    return
                }
                DreamStudioHandler.handle(config, engine, event)
            }
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        if (event.name == "askto") {
            event.replyChoiceStrings(models.filter { it.startsWith(event.focusedOption.value) }).queue()
        } else if (event.name == "predicate") {
            event.replyChoiceStrings(modelsToPrice.filter { it.startsWith(event.focusedOption.value) }).queue()
        } else if (event.name == "dream") {
            event.replyChoiceStrings(engines.filter { it.startsWith(event.focusedOption.value) }).queue()
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        if (event.member == null)
            return
        if (event.componentId == "delete_message") {
            if(event.message.embeds[0].fields.isEmpty()){
                event.message.delete().queue()
                return
            }
            if (event.message.embeds[0].fields[0]!!.value == event.member!!.asMention) {
                event.message.delete().queue()
            } else {
                event.reply("이미지 생성을 요청한 사람만 폭파가 가능합니다.").setEphemeral(true).queue()
            }
        }
    }
}