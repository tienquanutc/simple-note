package vertx

import com.fasterxml.jackson.annotation.JsonInclude
import groovy.transform.CompileStatic
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.validator.routines.InetAddressValidator

import java.lang.management.ManagementFactory
import java.lang.management.MemoryPoolMXBean
import java.lang.management.ThreadMXBean

abstract class VertxServer<T extends VertxConfig> {

    T config

    final Vertx vertx
    final Router router
    final HttpServerOptions httpServerOptions
    final HttpServer httpServer

    VertxServer(T config) {
        this.config = config
        this.vertx = config.vertx

        this.httpServerOptions = this.createHttpServerOptions()
        this.httpServer = this.vertx.createHttpServer(httpServerOptions)
        this.router = Router.router(config.vertx)

        Json.mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        router.route().last().handler(this.&notFound)
        router.route().failureHandler(this.&failure)

        router.get('/ping').handler(this.&ping)
        this.setupRouter()
    }

    abstract def setupRouter()

    @CompileStatic
    def ping(RoutingContext ctx) {
        if ('true' == ctx.request().getParam("gc")) {
            System.gc()
        }

        Runtime runtime = Runtime.getRuntime();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean()

        def resultMap = [
                heap   : [
                        used_heap: ((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)).round(1) + " MB",
                        heap_size: (runtime.totalMemory() / (1024 * 1024)).round(1) + " MB",
                        max_heap : (runtime.maxMemory() / (1024 * 1024)).round(1) + " MB"
                ],
                threads: [
                        live_threads  : threadMXBean.threadCount,
                        daemon_threads: threadMXBean.daemonThreadCount
                ]
        ]

        for (MemoryPoolMXBean memoryMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
            if ("Metaspace" == memoryMXBean.getName()) {
                resultMap.metaspace = [
                        used_metaspace: (memoryMXBean.getUsage().getUsed() / (1024 * 1024)).round(1) + " MB"
                ]
            }
        }

        resultMap.network = [
                ip: getIp(ctx.request())
        ]

        resultMap.request = [
                headers: ctx.request().headers().names().collectEntries {
                    [(it): ctx.request().headers().get(it)]
                }
        ]

        ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject(resultMap as Map<String, Object>).encode())
    }

    @CompileStatic
    def notFound(RoutingContext ctx) {
        Map resultMap = [
                status_code: 404,
                error      : [
                        message: "resource not found"
                ]
        ]

        ctx.response()
                .setStatusCode(404)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject(resultMap as Map<String, Object>).encode())
    }

    @CompileStatic
    def failure(RoutingContext ctx) {
        if (ctx.failure() == null) {
            ctx.next()
            return
        }
        def debug = ctx.request().getParam("debug")?.equalsIgnoreCase("true")
        def resultMap = [
                status_code: 500,
                error      : [
                        message           : ctx.failure().getMessage() ?: ExceptionUtils.getRootCauseMessage(ctx.failure()),
                        root_cause_message: debug ? ExceptionUtils.getRootCauseMessage(ctx.failure()) : null,
                        detail            : debug ? ExceptionUtils.getStackTrace(ctx.failure()) : null
                ]
        ]

        ctx.response()
                .setStatusCode(500)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject(resultMap as Map<String, Object>).encode())
    }

    HttpServerOptions createHttpServerOptions() {
        return new HttpServerOptions(
                maxInitialLineLength: 4096 * 4,
                compressionSupported: true,
                maxHeaderSize: 8192 * 2
        )
    }

    @CompileStatic
    void start(Handler<AsyncResult<HttpServer>> listenHandler) {
        httpServer.requestHandler(router.&accept).listen(config.httpPort, { event ->
            listenHandler?.handle(event)
            if (event.succeeded()) {
                println "Server started at http://127.0.0.1:" + httpServer.actualPort()
            } else {
                close()
                throw new RuntimeException("Unable to start Server at http://127.0.0.1:" + httpServer.actualPort(), event.cause())
            }
        })
    }


    void close() {
        httpServer?.close()
        this.vertx?.close()
    }

    static String getIp(HttpServerRequest request) {
        return getIp(request, "X-Forwarded-For")
    }

    @CompileStatic
    static String getIp(HttpServerRequest request, String header) {
        String ip = getIpFromHeader(request, header, false)
        if (StringUtils.isNotBlank(ip)) {
            return ip
        }
        return request.remoteAddress().host()
    }

    @CompileStatic
    static String getIpFromHeader(HttpServerRequest request, String header, boolean allowPrivate) {
        String value = request.getHeader(header)
        if (StringUtils.isBlank(value)) {
            return null
        }
        StringTokenizer tokenizer = new StringTokenizer(value, ",")
        while (tokenizer.hasMoreTokens()) {
            String ip = tokenizer.nextToken().trim()
            if (!InetAddressValidator.getInstance().isValid(ip)) {
                continue
            }
            if (!allowPrivate && isIPv4Private(ip)) {
                continue
            }
            return ip
        }

        return null
    }

    /**
     * Check private IP is v4
     * 10.0.0.0 ~ 10.255.255.255
     * 172.16.0.0 ~ 172.31.255.255
     * 192.168.0.0 ~ 192.168.255.255
     * @param ip
     * @return
     */
    @CompileStatic
    static boolean isIPv4Private(String ip) {
        if (!InetAddressValidator.instance.isValidInet4Address(ip)) {
            return false
        }
        String[] octets = ip.split("\\.")
        long longIp = (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16) + (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3])
        return (167772160L <= longIp && longIp <= 184549375L) || (2886729728L <= longIp && longIp <= 2887778303L) || (3232235520L <= longIp && longIp <= 3232301055L)
    }

}
