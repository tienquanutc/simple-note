package hanlder

import app.AppConfig
import vertx.VertxViewHandler

class FEATURE extends VertxViewHandler<AppConfig> {

    FEATURE(AppConfig config) {
        super(config, '/feature.peb')
    }
}
