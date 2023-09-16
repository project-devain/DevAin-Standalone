package skywolf46.devain.model.rest.openai.completion.request

import arrow.core.None
import arrow.core.Option

data class OpenAIFunctionDeclaration(
    val functionName: String,
    val source: Option<Pair<FunctionFlag, String>> = None
) {
    enum class FunctionFlag {
        BUILT_IN,
        USER_SPECIFIED,
        ROOM_SPECIFIED
    }
}