package skywolf46.devain.model.data.config

import skywolf46.devain.annotations.config.ConfigDefault
import skywolf46.devain.annotations.config.MarkConfigElement

data class APITokenElement(
    @MarkConfigElement
    @ConfigDefault.String("YOUR-API-TOKEN-HERE")
    val apiToken: String
) : ConfigElement