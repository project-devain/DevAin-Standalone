package skywolf46.devain.model

import arrow.core.Either
import org.koin.core.component.KoinComponent

interface Request<T : Any> : KoinComponent{
    fun asJson(): Either<Throwable, T>
}