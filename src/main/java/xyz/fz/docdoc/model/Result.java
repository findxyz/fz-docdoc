package xyz.fz.docdoc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.vertx.core.json.JsonObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {

    private boolean success;

    private String message;

    private Object data;

    private String redirect;

    public static JsonObject ofSuccess() {
        JsonObject result = new JsonObject();
        result.put("success", true);
        return result;
    }

    public static JsonObject ofData(Object data) {
        JsonObject result = new JsonObject();
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    public static JsonObject ofMessage(String message) {
        JsonObject result = new JsonObject();
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    public static JsonObject ofRedirect(String redirect) {
        JsonObject result = new JsonObject();
        result.put("success", false);
        result.put("redirect", redirect);
        result.put("message", "会话已过期，请重新登录");
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
}
