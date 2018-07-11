package xyz.fz.docdoc.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.util.BaseUtil;
import xyz.fz.docdoc.util.EventBusUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

    private static final String CONTENT_HTML = "text/html; charset=utf-8";

    private static final String CONTENT_JSON = "application/json; charset=utf-8";

    private static final String CUR_USER = "curUser";

    private static final String ADMIN_USER_NAME = "admin";

    private static final String ADMIN_REQUIRE_MESSAGE = "需要管理员权限";

    private static final String STATIC_HTML = ".html";

    private static SessionStore sessionStore;

    private static ConcurrentHashMap<String, String> loginUserMap = new ConcurrentHashMap<>();

    private int port;

    public HttpVerticle(int port) {
        this.port = port;
    }

    @Override
    public void start() throws Exception {
        super.start();

        HttpServer server = vertx.createHttpServer();

        // By default routes are matched in the order they are added to the router.
        Router router = Router.router(vertx);

        sessionHandler(router);
        staticHandler(router);
        bodyHandler(router);
        indexLoginLogoutHandler(router);
        docdocManageHandler(router);
        docdocMappingHandler(router);
        failureHandler(router);

        server.requestHandler(router::accept);

        server.listen(port);
    }

    private void sessionHandler(Router router) {
        router.route().handler(CookieHandler.create());
        sessionStore = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(sessionStore);
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

    private void bodyHandler(Router router) {
        router.route().handler(BodyHandler.create());
    }

    private static void loginUser(JsonObject curUser, Session session) {
        logoutUser(curUser.getString("userName"));
        session.put(CUR_USER, curUser);
        loginUserMap.put(curUser.getString("userName"), session.id());
    }

    private static void logoutUser(String userName) {
        if (loginUserMap.containsKey(userName)) {
            sessionStore.get(loginUserMap.get(userName), sessionAsyncResult -> {
                if (sessionAsyncResult.succeeded()) {
                    sessionAsyncResult.result().remove(CUR_USER);
                    loginUserMap.remove(userName);
                }
            });
        }
    }

    private void indexLoginLogoutHandler(Router router) {
        router.route("/").handler(routingContext -> {
            routingContext.response().putHeader("Content-Type", CONTENT_HTML).sendFile("webroot/login.html");
        });

        router.route("/doLogin").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response.putHeader("Content-Type", CONTENT_JSON);
            try {
                vertx.eventBus().send(ServiceVerticle.Address.USER_LOGIN.toString(), routingContext.getBodyAsJson(), asyncResult -> {
                    String result;
                    if (asyncResult.succeeded()) {
                        JsonObject curUser = new JsonObject(asyncResult.result().body().toString());
                        loginUser(curUser, routingContext.session());
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

        router.route("/doLogout").handler(routingContext -> {
            JsonObject curUser = routingContext.session().get(CUR_USER);
            if (curUser != null) {
                logoutUser(curUser.getString("userName"));
            }
            routingContext.response().putHeader("Content-Type", CONTENT_JSON).end(Result.ofSuccess().toString());
        });
    }

    private void docdocManageHandler(Router router) {

        /* ===================== manage html filter ===================== */
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

        /* ===================== manage api filter ===================== */
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

        homeHandler(router);
        userHandler(router);
        docHandler(router);
    }

    private void homeHandler(Router router) {
        router.route("/docdoc/manage/html/home").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/home.html");
        });

        router.route("/docdoc/manage/api/login/info").handler(routingContext -> {
            JsonObject curUserJsonObject = routingContext.session().get(CUR_USER);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("userName", curUserJsonObject.getValue("userName"));
            routingContext.response().end(Result.ofData(userMap).toString());
        });
    }

    private void userHandler(Router router) {

        /* ===================== manage html user filter ===================== */
        router.route("/docdoc/manage/html/user/*").handler(routingContext -> {
            JsonObject curUserJsonObject = routingContext.session().get(CUR_USER);
            String curUsername = curUserJsonObject.getString("userName");
            if (!ADMIN_USER_NAME.equals(curUsername)) {
                routingContext.response().end(ADMIN_REQUIRE_MESSAGE);
            } else {
                routingContext.next();
            }
        });

        /* ===================== manage api user filter ===================== */
        router.route("/docdoc/manage/api/user/*").handler(routingContext -> {
            JsonObject curUserJsonObject = routingContext.session().get(CUR_USER);
            String curUsername = curUserJsonObject.getString("userName");
            if (!ADMIN_USER_NAME.equals(curUsername)) {
                routingContext.response().end(Result.ofMessage(ADMIN_REQUIRE_MESSAGE).toString());
            } else {
                routingContext.next();
            }
        });

        router.route("/docdoc/manage/html/user/main").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/user/main.html");
        });

        router.route("/docdoc/manage/html/user/add").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/user/add.html");
        });

        router.route("/docdoc/manage/api/user/add").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.Address.USER_ADD);
        });

        router.route("/docdoc/manage/api/user/list").handler(routingContext -> {
            EventBusUtil.formBus(vertx, routingContext, ServiceVerticle.Address.USER_LIST);
        });

        router.route("/docdoc/manage/api/user/del").handler(routingContext -> {
            try {
                JsonObject eventJsonObject = routingContext.getBodyAsJson();
                vertx.eventBus().send(ServiceVerticle.Address.USER_DEL.toString(), eventJsonObject, asyncResult -> {
                    String result;
                    if (asyncResult.succeeded()) {
                        JsonObject replyJsonObject = new JsonObject(asyncResult.result().body().toString());
                        logoutUser(replyJsonObject.getString("data"));
                        result = asyncResult.result().body().toString();
                    } else {
                        result = Result.ofMessage(asyncResult.cause().getMessage()).toString();
                    }
                    routingContext.response().end(result);
                });
            } catch (Exception e) {
                LOGGER.error(BaseUtil.getExceptionStackTrace(e));
                routingContext.response().end(Result.ofMessage(e.getMessage()).toString());
            }
        });

        router.route("/docdoc/manage/api/user/admin/update").handler(routingContext -> {
            try {
                JsonObject eventJsonObject = routingContext.getBodyAsJson();
                vertx.eventBus().send(ServiceVerticle.Address.USER_ADMIN_UPDATE.toString(), eventJsonObject, asyncResult -> {
                    String result;
                    if (asyncResult.succeeded()) {
                        logoutUser("admin");
                        result = asyncResult.result().body().toString();
                    } else {
                        result = Result.ofMessage(asyncResult.cause().getMessage()).toString();
                    }
                    routingContext.response().end(result);
                });
            } catch (Exception e) {
                LOGGER.error(BaseUtil.getExceptionStackTrace(e));
                routingContext.response().end(Result.ofMessage(e.getMessage()).toString());
            }
        });
    }

    private void docHandler(Router router) {
        router.route("/docdoc/manage/html/doc/main").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/doc/main.html");
        });

        router.route("/docdoc/manage/html/doc/api/main").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/doc/api/main.html");
        });

        router.route("/docdoc/manage/html/doc/api/add").handler(routingContext -> {
            routingContext.response().sendFile("webroot/docdoc/manage/doc/api/add.html");
        });

        router.route("/docdoc/manage/api/doc/project/add").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.Address.DOC_PROJECT_ADD);
        });

        router.route("/docdoc/manage/api/doc/project/list").handler(routingContext -> {
            EventBusUtil.formBus(vertx, routingContext, ServiceVerticle.Address.DOC_PROJECT_LIST);
        });

        router.route("/docdoc/manage/api/doc/api/add").handler(routingContext -> {
            JsonObject curUserJsonObject = routingContext.session().get(CUR_USER);
            JsonObject authorJsonObject = new JsonObject();
            authorJsonObject.put("author", curUserJsonObject.getValue("userName"));
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.Address.DOC_API_ADD, authorJsonObject);
        });

        router.route("/docdoc/manage/api/doc/api/list").handler(routingContext -> {
            EventBusUtil.formBus(vertx, routingContext, ServiceVerticle.Address.DOC_API_LIST);
        });

        router.route("/docdoc/manage/api/doc/api/edit").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.Address.DOC_API_EDIT);
        });

        router.route("/docdoc/manage/api/doc/api/del").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.Address.DOC_API_DEL);
        });

        router.route("/docdoc/manage/api/doc/api/status").handler(routingContext -> {
            EventBusUtil.jsonBus(vertx, routingContext, ServiceVerticle.Address.DOC_API_STATUS);
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
