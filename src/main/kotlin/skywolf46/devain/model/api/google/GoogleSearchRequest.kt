package skywolf46.devain.model.api.google

import skywolf46.devain.model.EmptyJSONRequest

class GoogleSearchRequest(val query: String, val amount: Int) : EmptyJSONRequest()