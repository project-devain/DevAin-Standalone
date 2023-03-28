package skywolf46.devain.listener

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import skywolf46.devain.config.BotConfig
import skywolf46.devain.util.OpenAiRequest

object DallEImageHandler {
    fun handle(config: BotConfig,event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue { hook ->
            processRequest(hook, config, event.getOption("prompt")!!.asString)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun processRequest(hook: InteractionHook, config: BotConfig, prompt: String) {
        GlobalScope.launch {
            kotlin.runCatching {
                val result = OpenAiRequest.requestDallE(config.openAIToken, prompt)
                result.onLeft {
                    hook.sendMessage("OpenAI API가 오류를 반환하였습니다 : $it").queue()
                }.onRight {
                    hook.sendMessageEmbeds(
                        EmbedBuilder()
                            .setTitle(prompt)
                            .setImage(it.urls[0])
                            .build()
                    ).queue()
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}