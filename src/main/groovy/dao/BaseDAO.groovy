package dao

import groovy.util.logging.Slf4j
import io.vertx.ext.sql.UpdateResult
import sqllite.SqlLiteClient

import java.util.concurrent.CompletableFuture

@Slf4j
class BaseDAO {

    protected final TABLE_NAME
    protected SqlLiteClient sqliteClient

    BaseDAO(SqlLiteClient sqlLiteClient, String tableName) {
        this.sqliteClient = sqlLiteClient
        this.TABLE_NAME = tableName
    }

    CompletableFuture<List<Map>> finds(Map request) {
        List<String> select = request.select as List<String> ?: ['*']
        Map where = request.where as Map ?: [:]
        Map orderBy = request.orderBy as Map ?: [:]
        Integer skip = request.skip as Integer ?: 0
        Integer limit = request.limit as Integer ?: -1

        def sqlBuilder = new StringBuilder()
        def params = []
        sqlBuilder << 'SELECT ' << select.join(', ')
        sqlBuilder << ' FROM ' << this.TABLE_NAME

        if (where) {
            sqlBuilder << ' WHERE ' << where.keySet().join(' ')
            params.addAll(where.values())
        }

        if (orderBy) {
            sqlBuilder << ' ORDER BY ' << orderBy.collect { key, value -> " $key $value " }.join(', ')
        }

        sqlBuilder << ' LIMIT ' << skip << ', ' << limit

        String sql = sqlBuilder.toString()
        if (log.isDebugEnabled()) {
            log.debug("FIND $sql with $params")
        }

        return this.sqliteClient.queryWithParams(sql, params).thenApply { resultSet ->
            resultSet.rows.collect { it.mapTo(Map) }
        }
    }


    CompletableFuture<UpdateResult> update(Map request) {
        Map where = request.where as Map
        Map update = request.update as Map

        def sqlBuilder = new StringBuilder()
        def params = []

        sqlBuilder << ' UPDATE ' << this.TABLE_NAME

        sqlBuilder << ' SET ' << update.keySet().collect { it + '= ?' }.join(', ')
        params.addAll(update.values())

        sqlBuilder << ' WHERE ' << where.keySet().join(' ')
        params.addAll(where.values())

        String sql = sqlBuilder.toString()
        if (log.isDebugEnabled()) {
            log.debug("UPDATE $sql with $params")
        }

        return this.sqliteClient.updateWithParams(sql, params)
    }

    CompletableFuture<UpdateResult> insert(Map request) {
        def params = request.values() as List
        def sqlBuilder = new StringBuilder()
        sqlBuilder << " INSERT INTO " << this.TABLE_NAME
        sqlBuilder << " ( " << request.keySet().join(', ') << " ) "
        sqlBuilder << " VALUES( " << request.keySet().collect { '?' }.join(',') << " ) "

        String sql = sqlBuilder.toString()
        if (log.isDebugEnabled()) {
            log.debug("INSERT $sql with $params")
        }

        return this.sqliteClient.updateWithParams(sql, params)
    }

    CompletableFuture<UpdateResult> delete(Map request) {
        Map where = request.where as Map
        List params = where.values() as List

        def sqlBuilder = new StringBuilder()
        sqlBuilder << "DELETE FROM " << this.TABLE_NAME
        sqlBuilder << " WHERE  " << where.keySet().join(' ')


        String sql = sqlBuilder.toString()
        if (log.isDebugEnabled()) {
            log.debug("DELETE $sql with $params")
        }

        return this.sqliteClient.updateWithParams(sql, params)
    }

    CompletableFuture<Map> findFirst(Map request) {
        def firstRequest = new HashMap(request)
        firstRequest.limit = 1
        return this.finds(request).thenApply { it[0] }
    }
}
