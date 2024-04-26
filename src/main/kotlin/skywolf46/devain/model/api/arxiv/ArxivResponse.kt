package skywolf46.devain.model.api.arxiv

import skywolf46.devain.apicall.networking.Response

data class ArxivResponse(val articles: List<Article>) : Response {

    data class Article(val title: String, val summary: String, val url: String, val published: String, val updated: String)
}