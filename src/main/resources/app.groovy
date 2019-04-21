environments {
    prod {
        httpPort = 5000
        db {
            sqlite = 'jdbc:sqlite:db/master.db'
        }
    }
    dev {
        httpPort = 5000
        db {
            sqlite = 'jdbc:sqlite:db/dev.master.db'
        }
        System.properties.put("vertx.disableFileCaching", "true")
        System.properties.put("io.vertx.ext.web.TemplateEngine.disableCache", "true")
    }
}
