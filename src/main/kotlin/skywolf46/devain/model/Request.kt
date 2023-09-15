package skywolf46.devain.model

import arrow.core.Either

interface Request<T : Any> {
    fun asJson(): Either<Throwable, T>
}