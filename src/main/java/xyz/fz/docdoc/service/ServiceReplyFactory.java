package xyz.fz.docdoc.service;

import io.vertx.core.json.JsonObject;
import org.springframework.context.ApplicationContext;
import xyz.fz.docdoc.verticle.ServiceVerticle;

public class ServiceReplyFactory {

    private static volatile boolean init = false;

    private static UserService userService;

    public static void init(ApplicationContext context) {
        if (!init) {
            init = true;
            userService = context.getBean("userServiceImpl", UserService.class);
        }
    }

    public static JsonObject reply(String address, JsonObject jsonObject) {
        if (!init) {
            throw new RuntimeException("ServiceReplyFactory 没有初始化");
        }
        switch (address) {
            case ServiceVerticle.USER_ADD:
                return userService.add(jsonObject);
            case ServiceVerticle.USER_LOGIN:
                return userService.login(jsonObject);
            default:
                throw new RuntimeException("EventBus address not found");
        }
    }
}
