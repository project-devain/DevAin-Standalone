package skywolf46.devain.model.api.openai.completion.functions

import kotlinx.coroutines.delay
import org.json.simple.JSONObject
import skywolf46.devain.model.api.openai.OpenAIParameterSchema
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionDeclaration
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey

class DelayDeclaration : OpenAIFunctionDeclaration(
    OpenAIFunctionKey("delay", OpenAIFunctionKey.FunctionFlag.BUILT_IN),
    "주어진 시간동안 대기합니다. 입력되는 시간은 밀리세컨드(ms) 단위입니다.",
    listOf(
        OpenAIParameterSchema(
            "millisecond",
            "대기할 시간입니다.",
            "number",
            "true"
        )
    )
) {
    override suspend fun call(param: JSONObject): JSONObject {
        delay(param["millisecond"].toString().toDouble().toLong())
        return JSONObject()
    }
}