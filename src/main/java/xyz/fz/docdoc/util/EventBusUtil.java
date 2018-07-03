package xyz.fz.docdoc.util;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.service.ServiceReplyFactory;

public class EventBusUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(EventBusUtil.class);

    public static void jsonBus(Vertx vertx, RoutingContext routingContext, String address) {
        routingContext.request().bodyHandler(event -> {
            EventBusUtil.send(vertx, address, event, routingContext.response());
        });
    }

    private static void send(Vertx vertx, String address, Buffer event, final HttpServerResponse response) {
        try {
            vertx.eventBus().send(address, new JsonObject(event.toString()), asyncResult -> {
                receive(asyncResult, response);
            });
        } catch (Exception e) {
            LOGGER.error(BaseUtil.getExceptionStackTrace(e));
            response.end(Result.ofMessage(e.getMessage()));
        }
    }

    private static void receive(AsyncResult<Message<Object>> asyncResult, HttpServerResponse response) {
        String result;
        if (asyncResult.succeeded()) {
            JsonObject resultJsonObject = asyncResult.result().body() == null ? new JsonObject() : (JsonObject) asyncResult.result().body();
            result = Result.ofData(resultJsonObject.getMap());
        } else {
            result = Result.ofMessage(asyncResult.cause().getMessage());
        }
        response.end(result);
    }

    public static void consumer(Vertx vertx, String address) {
        vertx.eventBus().consumer(address, msg -> {
            try {
                msg.reply(ServiceReplyFactory.reply(address, (JsonObject) msg.body()));
            } catch (Exception e) {
                LOGGER.error(BaseUtil.getExceptionStackTrace(e));
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getMessage());
            }
        });
    }
}
