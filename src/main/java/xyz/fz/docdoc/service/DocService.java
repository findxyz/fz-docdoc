package xyz.fz.docdoc.service;

import io.vertx.core.json.JsonObject;

public interface DocService {
    JsonObject projectAdd(JsonObject jsonObject);

    JsonObject projectList(JsonObject jsonObject);
}
