package skywolf46.devain.util

import arrow.core.Either

interface Transformer<FROM, TO> {
    fun transform(from: FROM): Either<Throwable, TO>
}