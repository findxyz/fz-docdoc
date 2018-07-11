package xyz.fz.docdoc.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.verticle.ServiceVerticle;

import java.util.HashMap;
import java.util.Map;

public class EventBusUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(EventBusUtil.class);

    public static void jsonBus(Vertx vertx, RoutingContext routingContext, ServiceVerticle.Address address) {
        jsonBus(vertx, routingContext, address, null);
    }

    public static void jsonBus(Vertx vertx, RoutingContext routingContext, ServiceVerticle.Address address, JsonObject curUserJsonObject) {
        try {
            JsonObject eventJsonObject = routingContext.getBodyAsJson();
            if (curUserJsonObject != null) {
                eventJsonObject.mergeIn(curUserJsonObject);
            }
            send(vertx, address, eventJsonObject, routingContext.response());
        } catch (Exception e) {
            LOGGER.error(BaseUtil.getExceptionStackTrace(e));
            routingContext.response().end(Result.ofMessage(e.getMessage()).toString());
        }
    }

    public static void formBus(Vertx vertx, RoutingContext routingContext, ServiceVerticle.Address address) {
        formBus(vertx, routingContext, address, null);
    }

    public static void formBus(Vertx vertx, RoutingContext routingContext, ServiceVerticle.Address address, JsonObject curUserJsonObject) {
        Map<String, Object> formParams = new HashMap<>();
        MultiMap queryParams = routingContext.request().params();
        if (queryParams != null && queryParams.size() > 0) {
            for (Map.Entry<String, String> entry : queryParams) {
                formParams.put(entry.getKey(), entry.getValue());
            }
        }
        JsonObject jsonObject = new JsonObject(formParams);
        if (curUserJsonObject != null) {
            jsonObject.mergeIn(curUserJsonObject);
        }
        send(vertx, address, jsonObject, routingContext.response());
    }

    private static void send(Vertx vertx, ServiceVerticle.Address address, JsonObject jsonObject, final HttpServerResponse response) {
        vertx.eventBus().send(address.toString(), jsonObject, asyncResult -> {
            receive(asyncResult, response);
        });
    }

    private static void receive(AsyncResult<Message<Object>> asyncResult, HttpServerResponse response) {
        String result;
        if (asyncResult.succeeded()) {
            result = asyncResult.result().body().toString();
        } else {
            result = Result.ofMessage(asyncResult.cause().getMessage()).toString();
        }
        response.end(result);
    }
}
