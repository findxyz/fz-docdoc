package xyz.fz.docdoc.verticle;

import io.vertx.core.AbstractVerticle;
import xyz.fz.docdoc.util.EventBusUtil;

public class ServiceVerticle extends AbstractVerticle {

    public static final String USER_ADD = "USER_ADD";
    public static final String USER_LOGIN = "USER_LOGIN";

    @Override
    public void start() {
        EventBusUtil.consumer(vertx, USER_ADD);
        EventBusUtil.consumer(vertx, USER_LOGIN);
    }
}
