package skywolf46.devain.controller.api.requests.devain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.get
import skywolf46.devain.SQLITE_PERSISTENCE
import skywolf46.devain.TABLE_PERSISTENCE_COUNT
import skywolf46.devain.apicall.APICall
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.certainly
import skywolf46.devain.apicall.errors.UnexpectedError
import skywolf46.devain.apicall.networking.EmptyResponse
import skywolf46.devain.apicall.networking.GetRequest
import skywolf46.devain.model.api.openai.UpdateRequest
import skywolf46.devain.model.data.store.SqliteStore

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
        return withContext(Dispatchers.IO) {
            runCatching {
                val origin = apiCall.certainly(GetRequest(request.key))
                store.getConnection(SQLITE_PERSISTENCE)
                    .prepareStatement("replace into ${TABLE_PERSISTENCE_COUNT}(keyString, dataValue) values (?, ?)")
                    .use {
                        it.setString(1, request.key)
                        it.setLong(2, origin.value + request.delta)
                        it.execute()
                    }
                EmptyResponse().right()
            }.getOrElse {
                it.printStackTrace()
                UnexpectedError(it).left()
            }
        }
    }

}