package skywolf46.devain.model.api.openai

import skywolf46.devain.model.Response

data class GetResponse<T : Any>(val value: T) : Response