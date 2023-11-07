package skywolf46.devain.model

import arrow.core.Either
import arrow.core.right

open class EmptyRequest : Request<Unit> {
    override fun asJson(): Either<Throwable, Unit> {
        return Unit.right()
    }
}
