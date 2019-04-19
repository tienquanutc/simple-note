package app

import com.mitchellbosecke.pebble.PebbleEngine
import dao.NoteDAO
import io.*
import io.vertx.core.Vertx
import io.vertx.ext.web.templ.pebble.impl.PebbleTemplateEngineImpl
import io.vertx.ext.web.templ.pebble.impl.PebbleVertxLoader
import pebble.PebbleExtension
import sqllite.SqlLiteClient
import vertx.VertxConfig

class AppConfig extends VertxConfig {

    ConfigObject configObject

    NoteDAO noteDAO

    AppConfig(ConfigObject configObject) {
        this.configObject = configObject

        this.vertx = Vertx.vertx()
        this.httpPort = configObject.httpPort

        PebbleEngine pebbleEngine = new PebbleEngine.Builder()
                .loader(new PebbleVertxLoader(this.vertx))
                .extension(new PebbleExtension())
                .cacheActive(false)
                .build()
        this.templateEngine = new PebbleTemplateEngineImpl(pebbleEngine)

        SqlLiteClient sqlLiteClient = new SqlLiteClient(Vertx.vertx(), configObject.db.sqlite as String)
        this.noteDAO = new NoteDAO(sqlLiteClient)
    }
}
