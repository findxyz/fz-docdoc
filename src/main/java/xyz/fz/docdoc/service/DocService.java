package xyz.fz.docdoc.service;

import io.vertx.core.json.JsonObject;

public interface DocService {
    JsonObject projectAdd(JsonObject jsonObject);

    JsonObject projectEdit(JsonObject jsonObject);

    JsonObject projectDel(JsonObject jsonObject);

    JsonObject projectList(JsonObject jsonObject);

    JsonObject apiAdd(JsonObject jsonObject);

    JsonObject apiExampleUpdate(JsonObject jsonObject);

    JsonObject apiList(JsonObject jsonObject);

    JsonObject apiEdit(JsonObject jsonObject);

    JsonObject apiDel(JsonObject jsonObject);

    JsonObject apiStatus(JsonObject jsonObject);

    JsonObject apiFieldAdd(JsonObject jsonObject);

    JsonObject apiFieldDel(JsonObject jsonObject);

    JsonObject apiFieldOrder(JsonObject jsonObject);

    JsonObject apiFieldList(JsonObject jsonObject);

    JsonObject apiLogAdd(JsonObject jsonObject);

    JsonObject apiLogDel(JsonObject jsonObject);

    JsonObject apiLogList(JsonObject jsonObject);

    JsonObject apiResponseExampleAdd(JsonObject jsonObject);

    JsonObject apiResponseExampleDel(JsonObject jsonObject);

    JsonObject apiResponseExampleOne(JsonObject jsonObject);

    JsonObject apiMock(JsonObject jsonObject);
}
