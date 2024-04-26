package skywolf46.devain.model.api.openai.completion.functions

import org.json.simple.JSONObject
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionDeclaration
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey
import java.text.SimpleDateFormat

class TimeDeclaration : OpenAIFunctionDeclaration(
    OpenAIFunctionKey("time", OpenAIFunctionKey.FunctionFlag.BUILT_IN),
    "현재 시간을 한국시(GMT+9) 기준으로 불러옵니다.",
    emptyList()
) {
    private val timeFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss초")

    override suspend fun call(param: JSONObject): JSONObject {
        return JSONObject().apply {
            put("time", timeFormat.format(System.currentTimeMillis()))
        }
    }
}