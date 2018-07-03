package xyz.fz.docdoc.service;

import io.vertx.core.json.JsonObject;

public interface UserService {
    JsonObject add(JsonObject jsonObject);

    JsonObject login(JsonObject jsonObject);
}
