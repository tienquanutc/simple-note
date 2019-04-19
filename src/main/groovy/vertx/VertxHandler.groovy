package vertx;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public abstract class VertxHandler<T extends VertxConfig> implements Handler<RoutingContext> {

    T config;

    public VertxHandler(T config) {
        this.config = config;
    }

    @Override
    public void handle(RoutingContext event) {
    }

}
