package skywolf46.devain.util

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

fun JSONParser.parseMap(string: String) : JSONObject {
    return parse(string) as JSONObject
}

fun JSONParser.parseList(string: String) : JSONArray {
    return parse(string) as JSONArray
}