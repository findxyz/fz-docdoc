package xyz.fz.docdoc.service;

import io.vertx.core.json.JsonObject;

public interface DocService {
    JsonObject projectAdd(JsonObject jsonObject);

    JsonObject projectList(JsonObject jsonObject);

    JsonObject apiAdd(JsonObject jsonObject);

    JsonObject apiList(JsonObject jsonObject);

    JsonObject apiEdit(JsonObject jsonObject);

    JsonObject apiDel(JsonObject jsonObject);

    JsonObject apiStatus(JsonObject jsonObject);

    JsonObject apiFieldAdd(JsonObject jsonObject);
}
