package skywolf46.devain.model.rest.devain.data.response

import skywolf46.devain.model.Response

data class GetResponse<T : Any>(val value: T) : Response