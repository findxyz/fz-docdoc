package xyz.fz.docdoc.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
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

public class HttpVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

    private static final String LOGIN_PAGE = BaseUtil.urlLoading("/pubs/login.html");

    private static final String LOGIN_PATH = "login.html";

    private static final String HOME_PAGE = BaseUtil.urlLoading("/pubs/docdoc/manage/home.html");

    private static final String MANAGE_PATH = "docdoc/manage";

    private static final String CONTENT_HTML = "text/html; charset=utf-8";

    private static final String CONTENT_JSON = "application/json; charset=utf-8";

    private static final String CUR_USER = "curUser";

    @Override
    public void start() throws Exception {
        super.start();

        HttpServer server = vertx.createHttpServer();

        // By default routes are matched in the order they are added to the router.
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
            response.putHeader("Content-Type", CONTENT_HTML);
            Session session = routingContext.session();
            String uri = routingContext.request().uri();
            if (uri.contains(MANAGE_PATH) && session.get(CUR_USER) == null) {
                LOGGER.debug("redirect login");
                response.end(LOGIN_PAGE);
            } else if (uri.contains(LOGIN_PATH) && session.get(CUR_USER) != null) {
                response.end(HOME_PAGE);
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
            routingContext.response().putHeader("Content-Type", CONTENT_HTML).end(LOGIN_PAGE);
        });

        /* api filter */
        router.route("/*").handler(routingContext -> {
            LOGGER.debug("/* filter");
            HttpServerResponse response = routingContext.response();
            response.putHeader("Content-Type", CONTENT_JSON);
            routingContext.next();
        });

        /* api doLogin */
        router.route("/doLogin").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            routingContext.request().bodyHandler(event -> {
                try {
                    vertx.eventBus().send(ServiceVerticle.USER_LOGIN, new JsonObject(event.toString()), asyncResult -> {
                        String result;
                        if (asyncResult.succeeded()) {
                            routingContext.session().put(CUR_USER, asyncResult.result().body());
                            result = Result.ofSuccess();
                        } else {
                            result = Result.ofMessage(asyncResult.cause().getMessage());
                        }
                        response.end(result);
                    });
                } catch (Exception e) {
                    LOGGER.error(BaseUtil.getExceptionStackTrace(e));
                    response.end(Result.ofMessage(e.getMessage()));
                }
            });
        });

        /* api manage filter */
        router.route("/docdoc/manage/*").handler(routingContext -> {
            LOGGER.debug("/docdoc/manage filter");
            HttpServerResponse response = routingContext.response();
            Session session = routingContext.session();
            if (session.get(CUR_USER) == null) {
                LOGGER.debug("redirect login");
                response.end(Result.ofRedirect("login"));
            } else {
                routingContext.next();
            }
        });

        router.route("/docdoc/manage/user/add").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.USER_ADD);
        });

        server.requestHandler(router::accept);

        server.listen(Integer.parseInt(BaseProperties.get("server.port")));
    }
}
