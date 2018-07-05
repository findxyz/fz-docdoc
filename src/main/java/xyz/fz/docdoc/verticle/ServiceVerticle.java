package xyz.fz.docdoc.verticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.service.UserService;
import xyz.fz.docdoc.util.BaseUtil;

public class ServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVerticle.class);

    static final String USER_ADD = "USER_ADD";
    static final String USER_LOGIN = "USER_LOGIN";
    static final String USER_LIST = "USER_LIST";
    static final String USER_DEL = "USER_DEL";
    static final String USER_ADMIN_UPDATE = "USER_ADMIN_UPDATE";

    public ServiceVerticle(ApplicationContext context) {
        ReplyFactory.serviceInit(context);
    }

    @Override
    public void start() {
        consumer(vertx, USER_ADD);
        consumer(vertx, USER_LOGIN);
        consumer(vertx, USER_LIST);
        consumer(vertx, USER_DEL);
        consumer(vertx, USER_ADMIN_UPDATE);
    }

    private void consumer(Vertx vertx, String address) {
        vertx.eventBus().consumer(address, msg -> {
            try {
                JsonObject replyJsonObject = ReplyFactory.reply(address, (JsonObject) msg.body());
                String replyJsonString = replyJsonObject != null ? replyJsonObject.toString() : Result.ofSuccess().toString();
                msg.reply(replyJsonString);
            } catch (Exception e) {
                LOGGER.error(BaseUtil.getExceptionStackTrace(e));
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getMessage());
            }
        });
    }

    private static class ReplyFactory {

        private static volatile boolean init = false;

        private static UserService userService;

        public static void serviceInit(ApplicationContext context) {
            if (!init) {
                init = true;
                userService = context.getBean("userServiceImpl", UserService.class);
            }
        }

        static JsonObject reply(String address, JsonObject jsonObject) {
            if (!init) {
                throw new RuntimeException("ServiceReplyFactory 没有初始化");
            }
            switch (address) {
                case ServiceVerticle.USER_ADD:
                    return userService.add(jsonObject);
                case ServiceVerticle.USER_LOGIN:
                    return userService.login(jsonObject);
                case ServiceVerticle.USER_LIST:
                    return userService.list(jsonObject);
                case ServiceVerticle.USER_DEL:
                    return userService.del(jsonObject);
                case ServiceVerticle.USER_ADMIN_UPDATE:
                    return userService.adminUpdate(jsonObject);
                default:
                    throw new RuntimeException("EventBus address not found");
            }
        }
    }
}
