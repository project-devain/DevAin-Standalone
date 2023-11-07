package skywolf46.devain.model.api.openai.completion

import arrow.core.Either
import net.dv8tion.jda.api.entities.MessageEmbed
import skywolf46.devain.util.Transformer

object OpenAIGPTEmbedTransformer : Transformer<OpenAIGPTResponse, MessageEmbed> {
    override fun transform(from: OpenAIGPTResponse): Either<Throwable, MessageEmbed> {
        TODO("Not yet implemented")
    }

}