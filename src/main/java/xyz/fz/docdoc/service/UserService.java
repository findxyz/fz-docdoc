package xyz.fz.docdoc.service;

import io.vertx.core.json.JsonObject;

public interface UserService {
    boolean addUser(JsonObject jsonObject);
}
