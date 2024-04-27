package skywolf46.devain.model.api.google

import org.json.simple.JSONObject

data class GoogleSearchItem(
    val kind: String,
    val title: String,
    val htmlTitle: String,
    val link: String,
    val displayLink: String,
    val cacheId: String,
    val formattedUrl: String,
    val htmlFormattedUrl: String,
    val snippet: String,
    val htmlSnippet: String
) {
    companion object {
        fun fromJson(data: JSONObject): GoogleSearchItem {
            return GoogleSearchItem(
                data["kind"].toString(),
                data["title"].toString(),
                data["htmlTitle"].toString(),
                data["link"].toString(),
                data["displayLink"].toString(),
                data["cacheId"].toString(),
                data["formattedUrl"].toString(),
                data["htmlFormattedUrl"].toString(),
                data["snippet"].toString(),
                data["htmlSnippet"].toString(),
            )
        }
    }
}