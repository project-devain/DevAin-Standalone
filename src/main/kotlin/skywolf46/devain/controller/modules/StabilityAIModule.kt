package skywolf46.devain.controller.modules

import org.koin.core.component.inject
import skywolf46.devain.KEY_DALLE_PROCEED_COUNT
import skywolf46.devain.KEY_DALLE_PROCEED_TIME
import skywolf46.devain.apicall.certainly
import skywolf46.devain.apicall.networking.GetRequest
import skywolf46.devain.controller.api.requests.devain.DevAinPersistenceCountAPICall
import skywolf46.devain.platform.plugin.PluginModule
import skywolf46.devain.util.TimeUtil

class StabilityAIModule : PluginModule("Stability AI Integration") {
    private val apiCall by inject<DevAinPersistenceCountAPICall>()

    override fun onInitialize() {

    }

    override fun getVersion(): String {
        return "built-in"
    }

    override suspend fun getStatistics(): Map<String, List<PluginStatistics>> {
        return mutableMapOf(
            "Stable Diffusion" to listOf(
                PluginStatistics(
                    "처리 완료 개수",
                    "%,d".format(apiCall.certainly(GetRequest(KEY_DALLE_PROCEED_COUNT)).value)
                ),
                PluginStatistics(
                    "요청 처리 시간",
                    TimeUtil.toTimeString(apiCall.certainly(GetRequest(KEY_DALLE_PROCEED_TIME)).value)
                )
            )
        )
    }
}