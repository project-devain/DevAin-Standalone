package skywolf46.devain.model.api.openai.completion.functions

import arrow.core.getOrElse
import org.json.simple.JSONObject
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import skywolf46.devain.apicall.networking.GenericJSONObjectResponse
import skywolf46.devain.controller.api.requests.arxiv.ArxivSearchAPICall
import skywolf46.devain.model.api.arxiv.ArxivRequest
import skywolf46.devain.model.api.openai.OpenAIParameterSchema
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionDeclaration
import skywolf46.devain.model.api.openai.completion.OpenAIFunctionKey

class ArxivSearchDeclaration : OpenAIFunctionDeclaration(
    OpenAIFunctionKey("arxiv_search", OpenAIFunctionKey.FunctionFlag.BUILT_IN),
    "Arxiv API를 사용해 웹에서 검색 결과를 가져옵니다. 논문 제목에는 영어만 존재한다고 가정하고, 모든 쿼리를 영어로 제공하십시오. 결과에는 논문의 제목과 초록(Abstract) 및 논문이 제출된 날짜와 수정된 날짜가 포함됩니다.",
    listOf(
        OpenAIParameterSchema(
            "query",
            "검색할 쿼리입니다. 쿼리는 가능하면 최대한 짧고, 간결해야 합니다. 또한, 쿼리에는 검색 대상 논문에 대한 정보만 있어야 하며, 시간과 같은 부가 정보는 허용되지 않습니다.",
            "string",
            "true"
        )
    )
) {
    private val apiCall by inject<ArxivSearchAPICall>()

    override suspend fun call(param: JSONObject): JSONObject {
        return apiCall.call(ArxivRequest(param["query"].toString()))
            .map {
                JSONObject().apply {
                    put("result", it.articles.mapIndexed { index, item ->
                        JSONObject().apply {
                            put("title", item.title)
                            put("abstract", item.summary)
                            put("url", item.url)
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