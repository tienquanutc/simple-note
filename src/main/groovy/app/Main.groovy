package app

class Main {
    public static void main(String[] args) {
        def configEnv = System.getProperty('config.env')
        def configSlurper = new ConfigSlurper(configEnv ?: 'dev')
        def configObject = configSlurper.parse(Main.getResource("/app.groovy") as URL)

        def appConfig = new AppConfig(configObject)

        def server = new AppServer(appConfig)
        server.start {
            event -> //ignored
        }
    }
}
