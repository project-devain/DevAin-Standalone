package skywolf46.devain.model.rest.devain.data.request

import skywolf46.devain.model.EmptyRequest

data class GetRequest<T : Any>(val key: String) : EmptyRequest()