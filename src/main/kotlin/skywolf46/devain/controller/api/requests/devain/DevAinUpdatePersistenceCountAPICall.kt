package skywolf46.devain.controller.api.requests.devain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import org.koin.core.component.get
import skywolf46.devain.SQLITE_PERSISTENCE
import skywolf46.devain.TABLE_PERSISTENCE_COUNT
import skywolf46.devain.controller.api.APICall
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.certainly
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.model.EmptyResponse
import skywolf46.devain.model.rest.devain.data.request.GetRequest
import skywolf46.devain.model.rest.devain.data.request.UpdateRequest
import skywolf46.devain.model.store.SqliteStore

class DevAinUpdatePersistenceCountAPICall : APICall<UpdateRequest<Long>, EmptyResponse> {
    private val store = get<SqliteStore>()
    private val apiCall = get<DevAinPersistenceCountAPICall>()

    init {
        kotlin.runCatching {
            store.getConnection(SQLITE_PERSISTENCE)
                .prepareStatement("create table if not exists ${TABLE_PERSISTENCE_COUNT}(keyString VARCHAR(100) primary key, dataValue BIGINT)")
                .use {
                    it.execute()
                }
        }.onFailure {
            throw it
        }
    }

    override suspend fun call(request: UpdateRequest<Long>): Either<APIError, EmptyResponse> {
        return runCatching {
            val origin = apiCall.certainly(GetRequest(request.key))
            store.getConnection(SQLITE_PERSISTENCE)
                .prepareStatement("replace into ${TABLE_PERSISTENCE_COUNT}(keyString, dataValue) values (?, ?)")
                .use {
                    it.setString(1, request.key)
                    it.setLong(2, origin.value + request.delta)
                    it.execute()
                }
            return EmptyResponse().right()
        }.getOrElse {
            it.printStackTrace()
            UnexpectedError(it).left()
        }
    }

}