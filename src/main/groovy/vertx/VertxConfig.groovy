package vertx

import io.vertx.core.Vertx
import io.vertx.ext.web.common.template.TemplateEngine

class VertxConfig {
    int httpPort = 5000
    Vertx vertx
    String viewDirectory = "views"
    String webRootDirectory = "webroot"
    TemplateEngine templateEngine
}
