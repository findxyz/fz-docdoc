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

import java.util.HashMap;
import java.util.Map;

public class HttpVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

    private static final String CONTENT_HTML = "text/html; charset=utf-8";

    private static final String CONTENT_JSON = "application/json; charset=utf-8";

    private static final String CUR_USER = "curUser";

    private static final String ADMIN_USER_NAME = "admin";

    private static final String ADMIN_REQUIRE_MESSAGE = "需要管理员权限";

    private static final String STATIC_HTML = ".html";

    @Override
    public void start() throws Exception {
        super.start();

        HttpServer server = vertx.createHttpServer();

        // By default routes are matched in the order they are added to the router.
        Router router = Router.router(vertx);

        sessionHandler(router);
        staticHandler(router);
        indexLoginLogoutHandler(router);
        docdocManageHandler(router);
        docdocMappingHandler(router);
        failureHandler(router);

        server.requestHandler(router::accept);

        server.listen(Integer.parseInt(BaseProperties.get("server.port")));
    }

    private void sessionHandler(Router router) {
        router.route().handler(CookieHandler.create());
        SessionStore store = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(store);
        sessionHandler.setCookieHttpOnlyFlag(true);
        sessionHandler.setSessionTimeout(60 * 60 * 24 * 30 * 1000L);
        router.route().handler(sessionHandler);
    }

    private void staticHandler(Router router) {
        router.route("/pubs/*").handler(routingContext -> {
            if (routingContext.request().uri().contains(STATIC_HTML)) {
                routingContext.response().putHeader("location", "/").setStatusCode(302).end();
            } else {
                routingContext.next();
            }
        });
        router.route("/pubs/*").handler(StaticHandler.create());
    }

    private void indexLoginLogoutHandler(Router router) {
        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader("Content-Type", CONTENT_HTML).sendFile("webroot/login.html");
        });

        router.route("/doLogin").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("Content-Type", CONTENT_JSON);
            routingContext.request().bodyHandler(event -> {
                try {
                    vertx.eventBus().send(ServiceVerticle.USER_LOGIN, new JsonObject(event.toString()), asyncResult -> {
                        String result;
                        if (asyncResult.succeeded()) {
                            routingContext.session().put(CUR_USER, new JsonObject(asyncResult.result().body().toString()));
                            result = Result.ofSuccess().toString();
                        } else {
                            result = Result.ofMessage(asyncResult.cause().getMessage()).toString();
                        }
                        response.end(result);
                    });
                } catch (Exception e) {
                    LOGGER.error(BaseUtil.getExceptionStackTrace(e));
                    response.end(Result.ofMessage(e.getMessage()).toString());
                }
            });
        });

        router.route("/doLogout").handler(routingContext -> {
            routingContext.session().remove(CUR_USER);
            routingContext.response().putHeader("Content-Type", CONTENT_JSON).end(Result.ofSuccess().toString());
        });
    }

    private void docdocManageHandler(Router router) {

        /* ===================== html ===================== */
        router.route("/docdoc/manage/html/*").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("Content-Type", CONTENT_HTML);
            Session session = routingContext.session();
            if (session.get(CUR_USER) == null) {
                response.putHeader("location", "/").setStatusCode(302).end();
            } else {
                routingContext.next();
            }
        });

        router.route("/docdoc/manage/html/user/*").handler(routingContext -> {
            JsonObject curUserJsonObject = routingContext.session().get(CUR_USER);
            String curUsername = curUserJsonObject.getString("userName");
            if (!ADMIN_USER_NAME.equals(curUsername)) {
                routingContext.response().end(ADMIN_REQUIRE_MESSAGE);
            } else {
                routingContext.next();
            }
        });

        router.route("/docdoc/manage/html/home").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/home.html");
        });

        router.route("/docdoc/manage/html/user/main").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/user/main.html");
        });

        router.route("/docdoc/manage/html/user/add").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/user/add.html");
        });

        router.route("/docdoc/manage/html/api/main").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/api/main.html");
        });

        /* ===================== api ===================== */
        router.route("/docdoc/manage/api/*").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("Content-Type", CONTENT_JSON);
            Session session = routingContext.session();
            if (session.get(CUR_USER) == null) {
                response.end(Result.ofRedirect("login").toString());
            } else {
                routingContext.next();
            }
        });

        router.route("/docdoc/manage/api/login/info").handler(routingContext -> {
            JsonObject curUserJsonObject = routingContext.session().get(CUR_USER);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userName", curUserJsonObject.getValue("userName"));
            routingContext.response().end(Result.ofData(userMap).toString());
        });

        router.route("/docdoc/manage/api/user/*").handler(routingContext -> {
            JsonObject curUserJsonObject = routingContext.session().get(CUR_USER);
            String curUsername = curUserJsonObject.getString("userName");
            if (!ADMIN_USER_NAME.equals(curUsername)) {
                routingContext.response().end(Result.ofMessage(ADMIN_REQUIRE_MESSAGE).toString());
            } else {
                routingContext.next();
            }
        });

        router.route("/docdoc/manage/api/user/add").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.USER_ADD);
        });

        router.route("/docdoc/manage/api/user/list").handler(routingContext -> {
            EventBusUtil.formBus(vertx, routingContext, ServiceVerticle.USER_LIST);
        });

        router.route("/docdoc/manage/api/user/del").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.USER_DEL);
        });

        router.route("/docdoc/manage/api/user/admin/update").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.USER_ADMIN_UPDATE);
        });
    }

    private void docdocMappingHandler(Router router) {
        router.route("/*").handler(routingContext -> {
            routingContext.response().putHeader("Content-Type", CONTENT_JSON).end("docdocMapping");
        });
    }

    private void failureHandler(Router router) {
        router.route().failureHandler(routingContext -> {
            Throwable failure = routingContext.failure();
            LOGGER.error(BaseUtil.getExceptionStackTrace(failure));
            routingContext.response().end(Result.ofMessage(failure.getMessage()).toString());
        });
    }
}
