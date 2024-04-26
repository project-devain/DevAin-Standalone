package skywolf46.devain.model.api.google

import skywolf46.devain.apicall.networking.EmptyJSONRequest


class GoogleSearchRequest(val query: String, val amount: Int) : EmptyJSONRequest()