package xyz.fz.docdoc.service.impl;

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.fz.docdoc.dao.CommonDao;
import xyz.fz.docdoc.entity.*;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.repository.*;
import xyz.fz.docdoc.service.DocService;

import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocServiceImpl implements DocService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocServiceImpl.class);

    private final ProjectRepository projectRepository;

    private final ApiRepository apiRepository;

    private final ApiFieldRepository apiFieldRepository;

    private final ApiLogRepository apiLogRepository;

    private final ApiResponseExampleRepository apiResponseExampleRepository;

    private final CommonDao db;

    @Autowired
    public DocServiceImpl(ProjectRepository projectRepository,
                          ApiRepository apiRepository,
                          ApiFieldRepository apiFieldRepository,
                          ApiLogRepository apiLogRepository,
                          ApiResponseExampleRepository apiResponseExampleRepository,
                          CommonDao db) {
        this.projectRepository = projectRepository;
        this.apiRepository = apiRepository;
        this.apiFieldRepository = apiFieldRepository;
        this.apiLogRepository = apiLogRepository;
        this.apiResponseExampleRepository = apiResponseExampleRepository;
        this.db = db;
    }

    @Override
    public JsonObject projectAdd(JsonObject jsonObject) {
        Project project = new Project();
        project.setName(jsonObject.getString("name"));
        project.setIsActivity(1);
        projectRepository.save(project);
        return Result.ofSuccess();
    }

    @Override
    public JsonObject projectDel(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        Optional<Project> fProject = projectRepository.findById(id);
        if (fProject.isPresent()) {
            Project project = fProject.get();
            project.setIsActivity(0);
            projectRepository.save(project);
            return Result.ofSuccess();
        } else {
            throw new RuntimeException("项目不存在");
        }
    }

    @Override
    public JsonObject projectList(JsonObject jsonObject) {
        Project sProject = new Project();
        sProject.setIsActivity(1);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("isActivity", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<Project> projectExample = Example.of(sProject, exampleMatcher);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<Project> list = projectRepository.findAll(projectExample, sort);
        return Result.ofData(list);
    }

    @Override
    public JsonObject apiAdd(JsonObject jsonObject) {
        Long projectId = Long.valueOf(jsonObject.getValue("projectId", "-1").toString());
        Long id = Long.valueOf(jsonObject.getValue("id", "-1").toString());
        String name = jsonObject.getString("name");
        String requestUrl = jsonObject.getString("requestUrl");
        String authType = jsonObject.getString("authType");
        String contentType = jsonObject.getString("contentType");
        String requestMethod = jsonObject.getString("requestMethod");
        String dataType = jsonObject.getString("dataType");
        String author = jsonObject.getString("author");
        Api api;
        if (id > -1) {
            Optional<Api> fApi = apiRepository.findById(id);
            if (fApi.isPresent()) {
                api = fApi.get();
            } else {
                throw new RuntimeException("API文档不存在");
            }
        } else {
            api = new Api();
            api.setProjectId(projectId);
            api.setStatus(Api.STATUS_DEVELOP);
            api.setIsActivity(1);
            api.setCreateTime(new Date());
            api.setAuthor(author);
        }
        api.setName(name);
        api.setRequestUrl(requestUrl);
        api.setAuthType(authType);
        api.setContentType(contentType);
        api.setRequestMethod(requestMethod);
        api.setDataType(dataType);
        api.setUpdateTime(new Date());
        api = apiRepository.save(api);
        return Result.ofData(api.getId());
    }

    @Override
    public JsonObject apiExampleUpdate(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        String type = jsonObject.getString("type");
        String requestExample = jsonObject.getString("requestExample");
        String responseExample = jsonObject.getString("responseExample");
        Optional<Api> fApi = apiRepository.findById(id);
        if (fApi.isPresent()) {
            Api api = fApi.get();
            switch (type) {
                case "request":
                    api.setRequestExample(requestExample);
                    break;
                case "response":
                    api.setResponseExample(responseExample);
                    break;
                default:
            }
            api = apiRepository.save(api);
            return Result.ofData(api.getId());
        } else {
            throw new RuntimeException("API文档不存在");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject apiList(JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        String status = jsonObject.getString("status");
        Long projectId = Long.valueOf(jsonObject.getValue("projectId").toString());
        Map<String, Object> params = new HashMap<>();
        String sql = "";
        sql += "select d.id as id, d.name as name, d.requestUrl as requestUrl, d.status as status, d.updateTime as updateTime from t_doc_api d ";
        sql += "where 1=1 ";
        sql += "and d.isActivity = 1 ";
        sql += "and d.projectId = :projectId ";
        params.put("projectId", projectId);
        if (StringUtils.isNotBlank(name)) {
            sql += "and d.name like concat(:name, '%') ";
            params.put("name", name);
        }
        if (StringUtils.isNotBlank(status)) {
            sql += "and d.status = :status ";
            params.put("status", status);
        }
        List<Map> list = db.queryListBySql(sql, params, Map.class);
        if (list.size() > 0) {
            for (Map data : list) {
                data.put("updateTime", new DateTime(data.get("updateTime")).toString("yyyy-MM-dd HH:mm:ss"));
            }
        }
        JsonObject result = new JsonObject();
        result.put("code", 0);
        result.put("msg", "");
        result.put("data", list);
        result.put("count", list.size());
        return result;
    }

    @Override
    public JsonObject apiEdit(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        return Result.ofData(JsonObject.mapFrom(apiOne(id)));
    }

    private Api apiOne(Long id) {
        Optional<Api> fApi = apiRepository.findById(id);
        if (fApi.isPresent()) {
            return fApi.get();
        } else {
            throw new RuntimeException("API文档不存在");
        }
    }

    @Override
    public JsonObject apiDel(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        Optional<Api> fApi = apiRepository.findById(id);
        if (fApi.isPresent()) {
            Api api = fApi.get();
            api.setIsActivity(0);
            apiRepository.save(api);
            return Result.ofSuccess();
        } else {
            throw new RuntimeException("API文档不存在");
        }
    }

    @Override
    public JsonObject apiStatus(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        String status = jsonObject.getString("status");
        Optional<Api> fApi = apiRepository.findById(id);
        if (fApi.isPresent()) {
            Api api = fApi.get();
            api.setStatus(status);
            apiRepository.save(api);
            return Result.ofSuccess();
        } else {
            throw new RuntimeException("API文档不存在");
        }
    }

    @Override
    public JsonObject apiFieldAdd(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id", "-1").toString());
        Long apiId = Long.valueOf(jsonObject.getValue("apiId", "-1").toString());
        String actionType = jsonObject.getString("actionType");
        String meaning = jsonObject.getString("meaning");
        String name = jsonObject.getString("name");
        String paramType = jsonObject.getString("paramType");
        Integer required = Integer.valueOf(jsonObject.getValue("required").toString());
        ApiField apiField;
        if (id > -1) {
            Optional<ApiField> fApiField = apiFieldRepository.findById(id);
            if (fApiField.isPresent()) {
                apiField = fApiField.get();
            } else {
                throw new RuntimeException("API文档字段不存在");
            }
        } else {
            apiField = new ApiField();
            apiField.setApiId(apiId);
            apiField.setIsActivity(1);
            apiField.setVersion(System.currentTimeMillis() / 1000L);
        }
        apiField.setActionType(actionType);
        apiField.setMeaning(meaning);
        apiField.setName(name);
        apiField.setParamType(paramType);
        apiField.setRequired(required);
        apiField.setUpdateTime(new Date());
        apiField = apiFieldRepository.save(apiField);
        return Result.ofData(apiField.getId());
    }

    @Override
    public JsonObject apiFieldDel(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        Optional<ApiField> fApiField = apiFieldRepository.findById(id);
        if (fApiField.isPresent()) {
            ApiField apiField = fApiField.get();
            apiField.setIsActivity(0);
            apiFieldRepository.save(apiField);
            return Result.ofSuccess();
        } else {
            throw new RuntimeException("API文档字段不存在");
        }
    }

    @Override
    public JsonObject apiFieldOrder(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        String type = jsonObject.getString("type");
        Optional<ApiField> fApiField = apiFieldRepository.findById(id);
        if (fApiField.isPresent()) {
            ApiField curApiField = fApiField.get();
            Long curVersion = curApiField.getVersion();
            List<ApiField> list;
            switch (type) {
                case "up":
                    list = apiFieldRepository.findByVersionLessThanAndActionTypeAndIsActivityOrderByVersionDesc(curVersion, curApiField.getActionType(), 1);
                    break;
                case "down":
                    list = apiFieldRepository.findByVersionGreaterThanAndActionTypeAndIsActivityOrderByVersionAsc(curVersion, curApiField.getActionType(), 1);
                    break;
                default:
                    throw new RuntimeException("调整类型错误");
            }
            if (list != null && list.size() > 0) {
                ApiField swapApiField = list.get(0);
                Long swapVersion = swapApiField.getVersion();
                swapApiField.setVersion(curVersion);
                curApiField.setVersion(swapVersion);
                apiFieldRepository.save(swapApiField);
                apiFieldRepository.save(curApiField);
            }
            return Result.ofSuccess();
        } else {
            throw new RuntimeException("API文档字段不存在");
        }
    }

    @Override
    public JsonObject apiFieldList(JsonObject jsonObject) {
        Long apiId = Long.valueOf(jsonObject.getValue("apiId").toString());
        String actionType = jsonObject.getString("actionType");
        ApiField sApiField = new ApiField();
        sApiField.setIsActivity(1);
        sApiField.setApiId(apiId);
        sApiField.setActionType(actionType);
        sApiField.setVersion(null);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        Example<ApiField> apiFieldExample = Example.of(sApiField, exampleMatcher);
        Sort sort = Sort.by(Sort.Direction.ASC, "version");
        List<ApiField> list = apiFieldRepository.findAll(apiFieldExample, sort);

        JsonObject result = new JsonObject();
        result.put("code", 0);
        result.put("msg", "");
        result.put("data", list);
        result.put("count", list.size());
        return result;
    }

    @Override
    public JsonObject apiLogAdd(JsonObject jsonObject) {
        Long apiId = Long.valueOf(jsonObject.getValue("apiId").toString());
        String author = jsonObject.getString("author");
        String reason = jsonObject.getString("reason");
        ApiLog apiLog = new ApiLog();
        apiLog.setApiId(apiId);
        apiLog.setAuthor(author);
        apiLog.setCreateTime(new Date());
        apiLog.setReason(reason);
        apiLog.setIsActivity(1);
        apiLog = apiLogRepository.save(apiLog);
        return Result.ofData(apiLog.getId());
    }

    @Override
    public JsonObject apiLogDel(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        Optional<ApiLog> fApiLog = apiLogRepository.findById(id);
        if (fApiLog.isPresent()) {
            ApiLog apiLog = fApiLog.get();
            apiLog.setIsActivity(0);
            apiLogRepository.save(apiLog);
            return Result.ofSuccess();
        } else {
            throw new RuntimeException("API文档日志不存在");
        }
    }

    @Override
    public JsonObject apiLogList(JsonObject jsonObject) {
        Long apiId = Long.valueOf(jsonObject.getValue("apiId").toString());
        ApiLog sApiLog = new ApiLog();
        sApiLog.setIsActivity(1);
        sApiLog.setApiId(apiId);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        Example<ApiLog> apiLogExample = Example.of(sApiLog, exampleMatcher);
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<ApiLog> list = apiLogRepository.findAll(apiLogExample, sort);

        JsonObject result = new JsonObject();
        result.put("code", 0);
        result.put("msg", "");
        result.put("data", list);
        result.put("count", list.size());
        return result;
    }

    @Override
    public JsonObject apiResponseExampleAdd(JsonObject jsonObject) {
        Long apiId = Long.valueOf(jsonObject.getValue("apiId", "-1").toString());
        String ip = jsonObject.getString("ip");
        String responseExample = jsonObject.getString("responseExample");
        ApiResponseExample apiResponseExample;
        ApiResponseExample sApiResponseExample = new ApiResponseExample();
        sApiResponseExample.setApiId(apiId);
        sApiResponseExample.setIp(ip);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        Example<ApiResponseExample> apiResponseExampleExample = Example.of(sApiResponseExample, exampleMatcher);
        Optional<ApiResponseExample> fApiResponseExample = apiResponseExampleRepository.findOne(apiResponseExampleExample);
        if (fApiResponseExample.isPresent()) {
            apiResponseExample = fApiResponseExample.get();
        } else {
            apiResponseExample = new ApiResponseExample();
            apiResponseExample.setApiId(apiId);
        }
        apiResponseExample.setIp(ip);
        apiResponseExample.setResponseExample(responseExample);
        apiResponseExample.setUpdateTime(new Date());
        apiResponseExample = apiResponseExampleRepository.save(apiResponseExample);
        return Result.ofData(apiResponseExample.getId());
    }

    @Override
    public JsonObject apiResponseExampleDel(JsonObject jsonObject) {
        try {
            Long apiId = Long.valueOf(jsonObject.getValue("apiId").toString());
            String ip = jsonObject.getString("ip");
            ApiResponseExample sApiResponseExample = new ApiResponseExample();
            sApiResponseExample.setApiId(apiId);
            sApiResponseExample.setIp(ip);
            ExampleMatcher exampleMatcher = ExampleMatcher.matching();
            Example<ApiResponseExample> apiResponseExample = Example.of(sApiResponseExample, exampleMatcher);
            Optional<ApiResponseExample> fApiResponseExample = apiResponseExampleRepository.findOne(apiResponseExample);
            fApiResponseExample.ifPresent(apiResponseExampleRepository::delete);
            return Result.ofSuccess();
        } catch (Exception e) {
            throw new RuntimeException("API文档返回样例不存在");
        }
    }

    @Override
    public JsonObject apiResponseExampleOne(JsonObject jsonObject) {
        Long apiId = Long.valueOf(jsonObject.getValue("apiId").toString());
        String ip = jsonObject.getString("ip");
        ApiResponseExample sApiResponseExample = new ApiResponseExample();
        sApiResponseExample.setApiId(apiId);
        sApiResponseExample.setIp(ip);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        Example<ApiResponseExample> apiResponseExample = Example.of(sApiResponseExample, exampleMatcher);
        Optional<ApiResponseExample> fApiResponseExample = apiResponseExampleRepository.findOne(apiResponseExample);
        Map<String, Object> result = new HashMap<>();
        if (fApiResponseExample.isPresent()) {
            result.put("response", fApiResponseExample.get().getResponseExample());
            result.put("type", "custom");
            result.put("ip", fApiResponseExample.get().getIp());
            return Result.ofData(result);
        } else {
            result.put("response", apiOne(apiId).getResponseExample());
            result.put("type", "default");
            return Result.ofData(result);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject apiMock(JsonObject jsonObject) {
        String mockResult = "docdocNoMapping";
        String url = jsonObject.getString("url");
        Api sApi = new Api();
        sApi.setRequestUrl(url);
        sApi.setIsActivity(1);
        sApi.setVersion(null);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("requestUrl", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("isActivity", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<Api> apiExample = Example.of(sApi, exampleMatcher);
        Optional<Api> fApi = apiRepository.findOne(apiExample);
        if (fApi.isPresent()) {
            Api api = fApi.get();
            jsonObject.put("apiId", api.getId());
            JsonObject result = apiResponseExampleOne(jsonObject);
            Map<String, Object> dataMap = ((JsonObject) result.getValue("data")).getMap();
            if (dataMap != null) {
                mockResult = dataMap.get("response") != null ? dataMap.get("response").toString() : mockResult;
            }
        }
        return Result.ofData(mockResult);
    }
}
