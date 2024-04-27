package skywolf46.devain.controller.api.requests.arxiv

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.right
import io.ktor.client.*
import org.w3c.dom.Document
import org.w3c.dom.Node
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.XMLRESTAPICall
import skywolf46.devain.model.api.arxiv.ArxivRequest
import skywolf46.devain.model.api.arxiv.ArxivResponse

class ArxivSearchAPICall(client: Option<HttpClient> = None) : XMLRESTAPICall<ArxivRequest, ArxivResponse>({
    "https://export.arxiv.org/api/query?search_query=all:${it.query}&sortBy=relevance&sortOrder=descending&max_results=40"
}, client) {
    override suspend fun parseResult(request: ArxivRequest, response: Document): Either<APIError, ArxivResponse> {
        val elements = response.getElementsByTagName("entry")
        val list = mutableListOf<ArxivResponse.Article>()
        for (x in 0 until elements.length) {
            val node = elements.item(x).asMap()
            list += ArxivResponse.Article(
                node["title"]!!.replace("\n ", "").replace("\n", ""),
                node["summary"]!!.replace("\n ", "").replace("\n", ""),
                node["id"]!!,
                node["published"]!!,
                node["updated"]!!,
            )
        }
        return ArxivResponse(list).right()
    }

    private fun Node.asMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (x in 0 until childNodes.length) {
            val node = childNodes.item(x)
            map[node.nodeName] = node.textContent
        }
        return map
    }
}