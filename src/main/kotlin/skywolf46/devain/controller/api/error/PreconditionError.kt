package skywolf46.devain.controller.api.error

import skywolf46.devain.controller.api.APIError

open class PreconditionError(val exception: Throwable) : APIError {
    override fun getErrorMessage(): String {
        return exception.message!!
    }
}