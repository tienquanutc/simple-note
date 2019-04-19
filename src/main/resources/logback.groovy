import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.filter.ThresholdFilter

statusListener(NopStatusListener)
appender("STDOUT", ConsoleAppender) {
    layout(PatternLayout) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{100} - %msg%n"
    }
}

logger("io.netty", WARN)
logger("org.mongodb", WARN)

root(DEBUG, ["STDOUT"])