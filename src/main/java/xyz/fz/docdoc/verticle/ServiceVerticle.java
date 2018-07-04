package xyz.fz.docdoc.verticle;

import io.vertx.core.AbstractVerticle;
import xyz.fz.docdoc.util.EventBusUtil;

public class ServiceVerticle extends AbstractVerticle {

    public static final String USER_ADD = "USER_ADD";
    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String USER_LIST = "USER_LIST";
    public static final String USER_DEL = "USER_DEL";
    public static final String USER_ADMIN_UPDATE = "USER_ADMIN_UPDATE";

    @Override
    public void start() {
        EventBusUtil.consumer(vertx, USER_ADD);
        EventBusUtil.consumer(vertx, USER_LOGIN);
        EventBusUtil.consumer(vertx, USER_LIST);
        EventBusUtil.consumer(vertx, USER_DEL);
        EventBusUtil.consumer(vertx, USER_ADMIN_UPDATE);
    }
}
