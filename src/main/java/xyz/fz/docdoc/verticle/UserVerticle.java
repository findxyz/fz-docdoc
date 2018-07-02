package xyz.fz.docdoc.verticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.docdoc.service.UserService;
import xyz.fz.docdoc.util.BaseUtil;
import xyz.fz.docdoc.util.SpringContextHelper;

public class UserVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserVerticle.class);

    static final String ADD_USER = "ADD_USER";

    @Override
    public void start() {
        UserService userService = SpringContextHelper.getBean("userServiceImpl", UserService.class);

        EventBus eventBus = vertx.eventBus();

        eventBus.consumer(ADD_USER, msg -> {
            try {
                JsonObject requestMap = (JsonObject) msg.body();
                msg.reply(userService.addUser(requestMap));
            } catch (Exception e) {
                LOGGER.error(BaseUtil.getExceptionStackTrace(e));
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), BaseUtil.errorFormat(e.getMessage()));
            }
        });
    }
}
