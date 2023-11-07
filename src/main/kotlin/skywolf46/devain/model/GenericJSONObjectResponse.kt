package skywolf46.devain.model

import org.json.simple.JSONObject
import skywolf46.devain.model.EmptyResponse
import skywolf46.devain.model.Response

data class GenericJSONObjectResponse(val json: JSONObject) : Response