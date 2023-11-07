package skywolf46.devain.model.api.openai.completion

import arrow.core.Either
import skywolf46.devain.util.Transformer

object OpenAIGPTTextTransformer : Transformer<OpenAIGPTResponse, String> {
    override fun transform(from: OpenAIGPTResponse): Either<Throwable, String> {
        TODO()
    }

}