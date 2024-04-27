package skywolf46.devain.controller.api.requests.devain

import arrow.core.Either
import org.koin.core.component.get
import skywolf46.devain.SQLITE_PERSISTENCE
import skywolf46.devain.TABLE_PERSISTENCE_COUNT
import skywolf46.devain.apicall.APICall
import skywolf46.devain.apicall.APIError
import skywolf46.devain.apicall.networking.GenericListResponse
import skywolf46.devain.apicall.networking.GetRequest
import skywolf46.devain.model.data.store.SqliteStore

class DevAinPersistenceUserDataCall : APICall<GetRequest<String>, GenericListResponse<String>> {
    private val store = get<SqliteStore>()

    init {
        kotlin.runCatching {
            store.getConnection(SQLITE_PERSISTENCE)
                .prepareStatement("create table if not exists $TABLE_PERSISTENCE_COUNT(userId VARCHAR(100), keyString VARCHAR(100), sector(VARCHAR(100)), dataValue VARCHAR(100)) primary key(userId, keyString, sector)")
                .use {
                    it.execute()
                }
        }.onFailure {
            throw it
        }
    }

    override suspend fun call(request: GetRequest<String>): Either<APIError, GenericListResponse<String>> {
        TODO()
    }

}