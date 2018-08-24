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

import java.util.UUID;

public class ServiceVerticle extends AbstractVerticle {

    private String instanceId;

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceVerticle.class);

    public enum Address {

        /**
         * EventBus 通讯地址
         */
        USER_ADD("USER_ADD"),
        USER_LOGIN("USER_LOGIN"),
        USER_LIST("USER_LIST"),
        USER_DEL("USER_DEL"),
        USER_ADMIN_UPDATE("USER_ADMIN_UPDATE"),

        DOC_PROJECT_ADD("DOC_PROJECT_ADD"),
        DOC_PROJECT_DEL("DOC_PROJECT_DEL"),
        DOC_PROJECT_LIST("DOC_PROJECT_LIST"),
        DOC_API_ADD("DOC_API_ADD"),
        DOC_API_EXAMPLE_UPDATE("DOC_API_EXAMPLE_UPDATE"),
        DOC_API_LIST("DOC_API_LIST"),
        DOC_API_EDIT("DOC_API_EDIT"),
        DOC_API_DEL("DOC_API_DEL"),
        DOC_API_STATUS("DOC_API_STATUS"),
        DOC_API_FIELD_ADD("DOC_API_FIELD_ADD"),
        DOC_API_FIELD_DEL("DOC_API_FIELD_DEL"),
        DOC_API_FIELD_ORDER("DOC_API_FIELD_ORDER"),
        DOC_API_FIELD_LIST("DOC_API_FIELD_LIST"),
        DOC_API_LOG_ADD("DOC_API_LOG_ADD"),
        DOC_API_LOG_DEL("DOC_API_LOG_DEL"),
        DOC_API_LOG_LIST("DOC_API_LOG_LIST"),
        DOC_API_RESPONSE_EXAMPLE_ADD("DOC_API_RESPONSE_EXAMPLE_ADD"),
        DOC_API_RESPONSE_EXAMPLE_DEL("DOC_API_RESPONSE_EXAMPLE_DEL"),
        DOC_API_RESPONSE_EXAMPLE_ONE("DOC_API_RESPONSE_EXAMPLE_ONE");

        private String address;

        Address(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return address;
        }
    }

    public ServiceVerticle(ApplicationContext context) {
        ReplyFactory.serviceInit(context);
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
        private static DocService docService;

        private static void serviceInit(ApplicationContext context) {
            if (!init) {
                init = true;
                userService = context.getBean("userServiceImpl", UserService.class);
                docService = context.getBean("docServiceImpl", DocService.class);
            }
        }

        private static JsonObject reply(Address address, JsonObject jsonObject) {
            switch (address) {
                case USER_ADD:
                    return userService.add(jsonObject);
                case USER_LOGIN:
                    return userService.login(jsonObject);
                case USER_LIST:
                    return userService.list(jsonObject);
                case USER_DEL:
                    return userService.del(jsonObject);
                case USER_ADMIN_UPDATE:
                    return userService.adminUpdate(jsonObject);
                case DOC_PROJECT_ADD:
                    return docService.projectAdd(jsonObject);
                case DOC_PROJECT_DEL:
                    return docService.projectDel(jsonObject);
                case DOC_PROJECT_LIST:
                    return docService.projectList(jsonObject);
                case DOC_API_ADD:
                    return docService.apiAdd(jsonObject);
                case DOC_API_EXAMPLE_UPDATE:
                    return docService.apiExampleUpdate(jsonObject);
                case DOC_API_LIST:
                    return docService.apiList(jsonObject);
                case DOC_API_EDIT:
                    return docService.apiEdit(jsonObject);
                case DOC_API_DEL:
                    return docService.apiDel(jsonObject);
                case DOC_API_STATUS:
                    return docService.apiStatus(jsonObject);
                case DOC_API_FIELD_ADD:
                    return docService.apiFieldAdd(jsonObject);
                case DOC_API_FIELD_DEL:
                    return docService.apiFieldDel(jsonObject);
                case DOC_API_FIELD_ORDER:
                    return docService.apiFieldOrder(jsonObject);
                case DOC_API_FIELD_LIST:
                    return docService.apiFieldList(jsonObject);
                case DOC_API_LOG_ADD:
                    return docService.apiLogAdd(jsonObject);
                case DOC_API_LOG_DEL:
                    return docService.apiLogDel(jsonObject);
                case DOC_API_LOG_LIST:
                    return docService.apiLogList(jsonObject);
                case DOC_API_RESPONSE_EXAMPLE_ADD:
                    return docService.apiResponseExampleAdd(jsonObject);
                case DOC_API_RESPONSE_EXAMPLE_DEL:
                    return docService.apiResponseExampleDel(jsonObject);
                case DOC_API_RESPONSE_EXAMPLE_ONE:
                    return docService.apiResponseExampleOne(jsonObject);
                default:
                    throw new RuntimeException("EventBus address not found");
            }
        }
    }
}
