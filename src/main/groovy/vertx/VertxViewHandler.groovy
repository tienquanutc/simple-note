package vertx

import groovy.transform.CompileStatic
import io.vertx.core.AsyncResult
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.RoutingContext

class VertxViewHandler<T extends VertxConfig> extends VertxHandler<T> {

    String viewFilename

    VertxViewHandler(T config, String viewFilename) {
        super(config)
        this.viewFilename = viewFilename
    }

    @Override
    void handle(RoutingContext ctx) {
        this.renderView(ctx)
    }

    @CompileStatic
    void renderView(RoutingContext ctx) {
        this.renderView(ctx, this.viewFilename)
    }

    @CompileStatic
    void renderView(RoutingContext ctx, String viewFileName) {
        ctx.put("baseUrl", ctx.request().scheme() + "://" + ctx.request().host())
        ctx.put("absoluteUri", ctx.request().absoluteURI())

        try {
            config.templateEngine.render(ctx.data(), config.viewDirectory + viewFileName, { AsyncResult<Buffer> render ->
                if (render.failed()) throw render.cause()
                ctx.response().putHeader("Content-Type", "text/html; charset=UTF-8")
                ctx.response().end(render.result())
            })
        } catch (any) {
            any.printStackTrace()
        }
    }


}
