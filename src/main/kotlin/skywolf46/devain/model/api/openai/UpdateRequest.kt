package skywolf46.devain.model.api.openai

import skywolf46.devain.apicall.networking.EmptyRequest


data class UpdateRequest<DELTA : Any>(val key: String, val delta: DELTA) : EmptyRequest()