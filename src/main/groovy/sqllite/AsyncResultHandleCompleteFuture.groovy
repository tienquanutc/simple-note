package sqllite

import io.vertx.core.AsyncResult
import io.vertx.core.Handler

import java.util.concurrent.CompletableFuture

class AsyncResultHandleCompleteFuture<T> extends CompletableFuture implements Handler<AsyncResult<T>> {

    @Override
    void handle(AsyncResult<T> event) {
        event.succeeded() ? this.complete(event.result()) : this.completeExceptionally(event.cause())
    }
}
