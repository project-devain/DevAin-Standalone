package skywolf46.devain.model.api.arxiv

import arrow.core.Either
import arrow.core.right
import skywolf46.devain.apicall.networking.Request

data class ArxivRequest(val query: String) : Request<Any> {
    override fun serialize(): Either<Throwable, Any> {
        return "".right()
    }
}