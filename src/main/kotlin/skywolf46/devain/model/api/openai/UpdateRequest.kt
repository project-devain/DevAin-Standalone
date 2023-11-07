package skywolf46.devain.model.api.openai

import skywolf46.devain.model.EmptyRequest

data class UpdateRequest<DELTA : Any>(val key: String, val delta: DELTA) : EmptyRequest()