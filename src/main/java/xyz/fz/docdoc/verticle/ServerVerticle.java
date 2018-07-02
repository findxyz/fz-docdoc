package xyz.fz.docdoc.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.util.BaseProperties;
import xyz.fz.docdoc.util.BaseUtil;
import xyz.fz.docdoc.util.EventBusUtil;

public class ServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerVerticle.class);

    private static final String LOGIN_PAGE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>loading</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<script type=\"text/javascript\">\n" +
            "    window.location = \"/pubs/login.html\";\n" +
            "</script>\n" +
            "</body>\n" +
            "</html>";

    private boolean proEnv;

    public ServerVerticle(boolean proEnv) {
        this.proEnv = proEnv;
    }

    @Override
    public void start() throws Exception {
        super.start();

        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        /* session */
        router.route().handler(CookieHandler.create());
        SessionStore store = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(store);
        sessionHandler.setCookieHttpOnlyFlag(true);
        sessionHandler.setSessionTimeout(60 * 60 * 24 * 30 * 1000L);
        router.route().handler(sessionHandler);

        /* static */
        // static filter
        router.route("/pubs/*").handler(routingContext -> {
            LOGGER.debug("/pubs/* filter");
            HttpServerResponse response = routingContext.response();
            Session session = routingContext.session();
            String uri = routingContext.request().uri();
            if (session.get("curUser") == null && uri.contains("manage") && proEnv) {
                LOGGER.debug("redirect login");
                response.putHeader("content-type", "text/html").end(LOGIN_PAGE);
            } else {
                routingContext.next();
            }
        });
        router.route("/pubs/*").handler(StaticHandler.create());

        /* failure */
        router.route().failureHandler(routingContext -> {
            Throwable failure = routingContext.failure();
            LOGGER.error(BaseUtil.getExceptionStackTrace(failure));
            routingContext.response().end(Result.ofMessage(failure.getMessage()));
        });

        /* index */
        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader("content-type", "text/html").end(LOGIN_PAGE);
        });

        /* api base filter */
        router.route("/*").handler(routingContext -> {
            LOGGER.debug("/* filter");
            HttpServerResponse response = routingContext.response();
            response.putHeader("content-type", "application/json");
            routingContext.next();
        });

        /* api manage filter */
        router.route("/docdoc/manage/*").handler(routingContext -> {
            LOGGER.debug("/docdoc/manage filter");
            HttpServerResponse response = routingContext.response();
            Session session = routingContext.session();
            if (session.get("curUser") == null && proEnv) {
                LOGGER.debug("redirect login");
                response.end(Result.ofRedirect("login"));
            } else {
                routingContext.next();
            }
        });

        router.route("/docdoc/manage/addUser").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            routingContext.request().bodyHandler(event -> {
                EventBusUtil.eventBusSend(vertx, UserVerticle.ADD_USER, event, response);
            });
        });

        server.requestHandler(router::accept);

        server.listen(Integer.parseInt(BaseProperties.get("server.port")));
    }
}
