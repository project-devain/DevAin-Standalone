package skywolf46.devain.controller.api.error

import skywolf46.devain.controller.api.APIError

open class UnexpectedError(val exception: Throwable) : APIError {
    override fun getErrorMessage(): String {
        return "${exception.javaClass.name} : ${exception.message}"
    }
}