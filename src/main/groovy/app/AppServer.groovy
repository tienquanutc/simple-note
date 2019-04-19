package app

import hanlder.FEATURE

import hanlder.NOTE
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CookieHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.sstore.SessionStore;
import vertx.VertxServer;

class AppServer extends VertxServer<AppConfig> {

    public AppServer(AppConfig config) {
        super(config);
    }

    @Override
    def setupRouter() {

        router.route().handler(BodyHandler.create())
        router.route().handler(CookieHandler.create())
        router.route().handler(SessionHandler.create(SessionStore.create(vertx)))

        StaticHandler.create(config.webRootDirectory).with {
            router.getWithRegex("/favicon.ico").handler(delegate)
            router.getWithRegex("/.*").handler(delegate)
        }

        NOTE.newInstance(config).with {
            router.get("/").handler(delegate)

            router.get("/notes/all").handler(delegate.&handleAllNotes)
            router.get("/notes/all/").handler(delegate.&handleAllNotes)

            router.get("/notes").handler(delegate)
            router.get("/notes/").handler(delegate)

            router.get("/notes/:note_slug_url").handler(delegate)
            router.get("/notes/:note_slug_url/").handler(delegate)

            router.post("/notes/").handler(delegate.&handlePostNote)
            router.post("/notes").handler(delegate.&handlePostNote)
        }

        FEATURE.newInstance(config).with {
            router.get("/features").handler(delegate)
            router.get("/features/").handler(delegate)
        }


    }
}