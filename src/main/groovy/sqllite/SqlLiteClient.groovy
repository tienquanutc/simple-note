package sqllite

import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.SQLRowStream
import io.vertx.ext.sql.UpdateResult

import java.sql.ResultSet
import java.util.concurrent.CompletableFuture

class SqlLiteClient {

    JDBCClient delegate
    SQLConnection sqlConnection

    SqlLiteClient(Vertx vertx, String url) {
        Class.forName("org.sqlite.JDBC");
        def clientConfig = [url: url, driver_class: 'org.sqlite.JDBC'] as JsonObject
        this.delegate = JDBCClient.createShared(vertx, clientConfig)
        //sync connection
        this.sqlConnection = this.getConnection().get()
    }

    CompletableFuture<io.vertx.ext.sql.ResultSet> query(String sql) {
        def future = new AsyncResultHandleCompleteFuture<>()
        this.sqlConnection.query(sql, future)
        return future
    }

    CompletableFuture<io.vertx.ext.sql.ResultSet> queryWithParams(String sql, List params) {
        def future = new AsyncResultHandleCompleteFuture<io.vertx.ext.sql.ResultSet>()
        this.sqlConnection.queryWithParams(sql, params as JsonArray, future)
        return future
    }

    CompletableFuture<UpdateResult> update(String sql) {
        def future = new AsyncResultHandleCompleteFuture<UpdateResult>()
        this.sqlConnection.update(sql, future)
        return future
    }

    CompletableFuture<UpdateResult> updateWithParams(String sql, List params) {
        def future = new AsyncResultHandleCompleteFuture<UpdateResult>()
        this.sqlConnection.updateWithParams(sql, params as JsonArray, future)
        return future
    }

    CompletableFuture<UpdateResult> delete(String sql) {
        def future = new AsyncResultHandleCompleteFuture<UpdateResult>()
        this.sqlConnection.update(sql, future)
        return future
    }

    CompletableFuture<ResultSet> call(String sql) {
        def future = new AsyncResultHandleCompleteFuture<ResultSet>()
        this.sqlConnection.call(sql, future)
        return future
    }


    CompletableFuture<Void> execute(String sql) {
        def future = new AsyncResultHandleCompleteFuture<Void>()
        this.sqlConnection.execute(sql, future)
        return future
    }

    CompletableFuture<SQLRowStream> queryStream(String sql) {
        def future = new AsyncResultHandleCompleteFuture<SQLRowStream>()
        this.sqlConnection.queryStream(sql, future)
        return future
    }

    CompletableFuture<SQLRowStream> queryStreamWithParams(String sql, List params) {
        def future = new AsyncResultHandleCompleteFuture<SQLRowStream>()
        this.sqlConnection.queryStreamWithParams(sql, params as JsonArray, future)
        return future
    }

    CompletableFuture<SQLConnection> getConnection() {
        def future = new AsyncResultHandleCompleteFuture<SQLConnection>()
        this.delegate.getConnection(future)
        return future
    }

    CompletableFuture<Void> close() {
        def future = new AsyncResultHandleCompleteFuture<Void>()
        this.sqlConnection.close()
        delegate.close(future)
        return future
    }

    CompletableFuture<Void> commit() {
        def future = new AsyncResultHandleCompleteFuture<Void>()
        this.sqlConnection.commit(future)
        return future
    }

    CompletableFuture<Void> rollback() {
        def future = new AsyncResultHandleCompleteFuture<Void>()
        this.sqlConnection.rollback(future)
        return future
    }

}
