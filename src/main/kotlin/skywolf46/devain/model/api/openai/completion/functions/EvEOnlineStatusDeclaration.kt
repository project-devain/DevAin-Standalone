package skywolf46.devain.model.api.openai.completion.functions

import arrow.core.getOrElse
import org.json.simple.JSONObject
import org.koin.core.component.inject
import skywolf46.devain.controller.api.requests.eve.EvEOnlineStatusAPICall
import skywolf46.devain.model.EmptyJSONRequest
import skywolf46.devain.model.GenericJSONObjectResponse
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionDeclaration
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey

class EvEOnlineStatusDeclaration : OpenAIFunctionDeclaration(
    OpenAIFunctionKey("eve-status", OpenAIFunctionKey.FunctionFlag.BUILT_IN),
    "EvE Online(이브 온라인)의 현재 상태를 불러옵니다.",
    listOf()
){

    private val apiCall by inject<EvEOnlineStatusAPICall>()

    override suspend fun call(param: JSONObject): JSONObject {
        return apiCall.call(EmptyJSONRequest()).getOrElse {
            GenericJSONObjectResponse(JSONObject().apply {
                put("error", it.getErrorMessage())
            })
        }.json
    }

}