package skywolf46.devain.util

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

fun JSONParser.parseMap(string: String): JSONObject {
    return parse(string) as JSONObject
}

fun JSONParser.parseList(string: String): JSONArray {
    return parse(string) as JSONArray
}

fun JSONObject.getMap(key: String): JSONObject {
    return get(key) as JSONObject
}

fun JSONObject.getList(key: String): JSONArray {
    return get(key) as JSONArray
}

fun JSONArray.getMap(index: Int): JSONObject {
    return get(index) as JSONObject
}

fun JSONArray.getList(index: Int): JSONArray {
    return get(index) as JSONArray
}

fun JSONObject.putArray(key: String, vararg data: Any): JSONObject {
    put(key, JSONArray().apply {
        data.forEach {
            add(it)
        }
    })
    return this
}

fun JSONObject.putMap(key: String, vararg data: Pair<String, Any>): JSONObject {
    put(key, JSONObject().apply {
        data.forEach {
            put(it.first, it.second)
        }
    })
    return this
}


fun JSONArray.addArray(vararg data: Any): JSONArray {
    add(JSONArray().apply {
        data.forEach {
            add(it)
        }
    })
    return this
}

fun JSONArray.addMap(vararg data: Pair<String, Any>): JSONArray {
    add(JSONObject().apply {
        data.forEach {
            put(it.first, it.second)
        }
    })
    return this
}