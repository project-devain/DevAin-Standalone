package skywolf46.devain.model.rest.devain.data.request

import skywolf46.devain.model.EmptyRequest

data class UpdateRequest<DELTA: Any>(val key: String, val delta: DELTA) : EmptyRequest()