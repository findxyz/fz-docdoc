package xyz.fz.docdoc.verticle;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.service.DocService;
import xyz.fz.docdoc.service.UserService;
import xyz.fz.docdoc.util.BaseUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ServiceVerticle extends AbstractVerticle {

    private String instanceId;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVerticle.class);

    public enum Address {

        /**
         * EventBus 通讯地址
         */
        USER_ADD("USER_ADD", UserService.class, "add"),
        USER_LOGIN("USER_LOGIN", UserService.class, "login"),
        USER_LIST("USER_LIST", UserService.class, "list"),
        USER_DEL("USER_DEL", UserService.class, "del"),
        USER_ADMIN_UPDATE("USER_ADMIN_UPDATE", UserService.class, "adminUpdate"),

        DOC_PROJECT_ADD("DOC_PROJECT_ADD", DocService.class, "projectAdd"),
        DOC_PROJECT_EDIT("DOC_PROJECT_EDIT", DocService.class, "projectEdit"),
        DOC_PROJECT_DEL("DOC_PROJECT_DEL", DocService.class, "projectDel"),
        DOC_PROJECT_LIST("DOC_PROJECT_LIST", DocService.class, "projectList"),
        DOC_API_ADD("DOC_API_ADD", DocService.class, "apiAdd"),
        DOC_API_EXAMPLE_UPDATE("DOC_API_EXAMPLE_UPDATE", DocService.class, "apiExampleUpdate"),
        DOC_API_LIST("DOC_API_LIST", DocService.class, "apiList"),
        DOC_API_EDIT("DOC_API_EDIT", DocService.class, "apiEdit"),
        DOC_API_DEL("DOC_API_DEL", DocService.class, "apiDel"),
        DOC_API_STATUS("DOC_API_STATUS", DocService.class, "apiStatus"),
        DOC_API_FIELD_ADD("DOC_API_FIELD_ADD", DocService.class, "apiFieldAdd"),
        DOC_API_FIELD_DEL("DOC_API_FIELD_DEL", DocService.class, "apiFieldDel"),
        DOC_API_FIELD_ORDER("DOC_API_FIELD_ORDER", DocService.class, "apiFieldOrder"),
        DOC_API_FIELD_LIST("DOC_API_FIELD_LIST", DocService.class, "apiFieldList"),
        DOC_API_LOG_ADD("DOC_API_LOG_ADD", DocService.class, "apiLogAdd"),
        DOC_API_LOG_DEL("DOC_API_LOG_DEL", DocService.class, "apiLogDel"),
        DOC_API_LOG_LIST("DOC_API_LOG_LIST", DocService.class, "apiLogList"),
        DOC_API_RESPONSE_EXAMPLE_ADD("DOC_API_RESPONSE_EXAMPLE_ADD", DocService.class, "apiResponseExampleAdd"),
        DOC_API_RESPONSE_EXAMPLE_DEL("DOC_API_RESPONSE_EXAMPLE_DEL", DocService.class, "apiResponseExampleDel"),
        DOC_API_RESPONSE_EXAMPLE_ONE("DOC_API_RESPONSE_EXAMPLE_ONE", DocService.class, "apiResponseExampleOne"),
        DOC_API_MOCK("DOC_API_MOCK", DocService.class, "apiMock");

        private String address;

        private Class<?> clazz;

        private String method;

        Address(String address, Class<?> clazz, String method) {
            this.address = address;
            this.clazz = clazz;
            this.method = method;
        }

        @Override
        public String toString() {
            return address;
        }
    }

    private ServiceReply serviceReply;

    public ServiceVerticle(ApplicationContext context) {
        serviceReply = new ServiceReply(context);
        instanceId = UUID.randomUUID().toString();
    }

    @Override
    public void start() {
        Address[] allAddress = Address.values();
        for (Address address : allAddress) {
            consumer(vertx, address);
        }
    }

    private void consumer(Vertx vertx, Address address) {
        vertx.eventBus().consumer(address.toString(), msg -> {
            try {
                LOGGER.debug("verticle instanceId: {}, consumer address: {}", instanceId, address.toString());
                JsonObject replyJsonObject = serviceReply.reply(address, (JsonObject) msg.body());
                String replyJsonString = replyJsonObject != null ? replyJsonObject.toString() : Result.ofSuccess().toString();
                msg.reply(replyJsonString);
            } catch (Exception e) {
                LOGGER.error(BaseUtil.getExceptionStackTrace(e));
                msg.fail(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), e.getMessage());
            }
        });
    }

    private class ServiceReply {

        private ApplicationContext springContext;

        private ServiceReply(ApplicationContext springContext) {
            this.springContext = springContext;
        }

        private JsonObject reply(Address address, JsonObject jsonObject) {
            try {
                Object replyService = springContext.getBean(address.clazz);
                Method replyMethod = address.clazz.getMethod(address.method, JsonObject.class);
                return (JsonObject) replyMethod.invoke(replyService, jsonObject);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getTargetException().getMessage());
            } catch (NoSuchMethodException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
