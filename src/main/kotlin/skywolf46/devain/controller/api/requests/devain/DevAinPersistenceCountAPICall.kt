package skywolf46.devain.controller.api.requests.devain

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.get
import skywolf46.devain.SQLITE_PERSISTENCE
import skywolf46.devain.TABLE_PERSISTENCE_COUNT
import skywolf46.devain.controller.api.APICall
import skywolf46.devain.controller.api.APIError
import skywolf46.devain.controller.api.error.UnexpectedError
import skywolf46.devain.model.api.openai.GetRequest
import skywolf46.devain.model.api.openai.GetResponse
import skywolf46.devain.model.store.SqliteStore

class DevAinPersistenceCountAPICall : APICall<GetRequest<Long>, GetResponse<Long>> {
    private val store = get<SqliteStore>()

    init {
        kotlin.runCatching {
            store.getConnection(SQLITE_PERSISTENCE)
                .prepareStatement("create table if not exists $TABLE_PERSISTENCE_COUNT(keyString VARCHAR(100) primary key, dataValue BIGINT)")
                .use {
                    it.execute()
                }
        }.onFailure {
            throw it
        }
    }

    override suspend fun call(request: GetRequest<Long>): Either<APIError, GetResponse<Long>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                store.getConnection(SQLITE_PERSISTENCE)
                    .prepareStatement("select * from $TABLE_PERSISTENCE_COUNT where keyString = ?")
                    .use {
                        it.setString(1, request.key)
                        it.executeQuery().use { result ->
                            if (result.next())
                                GetResponse(result.getLong(2))
                            else
                                GetResponse(0L)

                        }
                    }.right()
            }.getOrElse { UnexpectedError(it).left() }
        }
    }

}