package skywolf46.devain.model.api.openai.completion.functions

import arrow.core.getOrElse
import org.json.simple.JSONObject
import org.koin.core.component.inject
import skywolf46.devain.apicall.networking.GenericJSONObjectResponse
import skywolf46.devain.controller.api.requests.google.GoogleSearchAPICall
import skywolf46.devain.model.api.google.GoogleSearchRequest
import skywolf46.devain.model.api.openai.OpenAIParameterSchema
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionDeclaration
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey

class GoogleSearchDeclaration : OpenAIFunctionDeclaration(
    OpenAIFunctionKey("google_search", OpenAIFunctionKey.FunctionFlag.BUILT_IN),
    "Google API를 사용해 웹에서 검색 결과를 가져옵니다. 쿼리는 영어로 요청하는 것이 더 정확한 결과를 반환합니다. 결과에는 링크와 페이지의 제목, 그리고 스니펫만이 포함되어 있습니다. 반환 결과에는 링크가 포함되나, 최대한 답변에는 링크 사용을 지양해야 합니다. 링크가 너무 긴 경우, 링크가 생략됩니다.",
    listOf(
        OpenAIParameterSchema(
            "query",
            "검색할 쿼리입니다.",
            "string",
            "true"
        )
    )
) {
    private val apiCall by inject<GoogleSearchAPICall>()

    override suspend fun call(param: JSONObject): JSONObject {
        return apiCall.call(GoogleSearchRequest(param["query"].toString(), 6))
            .map {
                JSONObject().apply {
                    put("result", it.items.mapIndexed { _, item ->
                        JSONObject().apply {
                            put("title", item.title)
                            if (item.link.length < 50)
                                put("link", item.link)
//                                put("link", "REF#${index}")
                            put("snippet", item.snippet)
                        }
                    })
                }
            }
            .mapLeft {
                GenericJSONObjectResponse(JSONObject().apply {
                    put("error", it.getErrorMessage())
                })
            }
            .getOrElse { it.json }
    }
}