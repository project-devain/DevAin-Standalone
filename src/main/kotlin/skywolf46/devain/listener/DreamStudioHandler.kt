package skywolf46.devain.listener

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.FileUpload
import skywolf46.devain.config.BotConfig
import skywolf46.devain.data.dreamstudio.DreamStudioRequestData
import skywolf46.devain.util.DreamStudioRequest
import java.util.*

object DreamStudioHandler {
    val engines = mutableListOf<String>(
        "stable-diffusion-v1-5",
        "stable-diffusion-512-v2-1",
        "stable-diffusion-768-v2-1"
    )

    fun handle(config: BotConfig, engine: String, event: SlashCommandInteractionEvent) {
        event.deferReply(false).queue { hook ->
            processRequest(
                event,
                hook,
                engine,
                DreamStudioRequestData(
                    listOf(event.getOption("prompt")!!.asString to 1.0),
                    steps = event.getOption("step")?.asInt ?: 40,
                    cfgScale = event.getOption("cfg_scale")?.asDouble ?: 7.0
                ),
                config
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun processRequest(
        event: SlashCommandInteractionEvent,
        hook: InteractionHook,
        engine: String,
        request: DreamStudioRequestData,
        config: BotConfig
    ) {

        GlobalScope.launch {
            kotlin.runCatching {
                val result = DreamStudioRequest.requestGenerateImage(config.dreamStudioToken, engine, request)
                result.onLeft {
                    hook.sendMessage("DreamBooth API가 오류를 반환하였습니다 : $it").queue()
                }.onRight {
                    when (it.finishReason) {
                        "SUCCESS" -> {
                            val image = Base64.getDecoder().decode(it.base64Image)
                            hook.sendMessageEmbeds(
                                EmbedBuilder()
                                    .setTitle("이미지 생성됨")
                                    .addField("이미지 생성 요청", event.member?.asMention ?: "알 수 없음", false)
                                    .addField("프롬프트", request.prompt[0].first, false)
                                    .setFooter("$engine (CGF ${request.cfgScale}, Step ${request.steps}, ${request.clip_guidance_preset})")
                                    .build()
                            ).addFiles(FileUpload.fromData(image, "image.png").asSpoiler())
                                .addActionRow(
                                    Button.danger("delete_message", "폭파")
                                )
                                .queue()
                        }

                        "ERROR" -> {
                            hook.sendMessage("이미지 생성에 실패하였습니다 : DreamStudio API에서 오류가 발생하였습니다.").queue()
                        }

                        "CONTENT_FILTERED" -> {
                            hook.sendMessage("이미지 생성에 실패하였습니다 : DreamStudio API에 의해 검열되었습니다.").queue()
                        }
                    }
                }
            }.onFailure {
                hook.sendMessage("OpenAI API와 통신 중 오류가 발생하였습니다. (${it.javaClass.simpleName} : ${it.message})").queue()
                it.printStackTrace()
            }
        }
    }
}