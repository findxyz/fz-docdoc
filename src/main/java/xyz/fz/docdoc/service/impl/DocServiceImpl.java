package xyz.fz.docdoc.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.fz.docdoc.entity.*;
import xyz.fz.docdoc.model.MockUrl;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.repository.*;
import xyz.fz.docdoc.service.DocService;
import xyz.fz.docdoc.util.BaseUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Transactional(rollbackFor = Exception.class)
public class DocServiceImpl implements DocService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocServiceImpl.class);

    private final ProjectRepository projectRepository;

    private final ApiRepository apiRepository;

    private final ApiFieldRepository apiFieldRepository;

    private final ApiLogRepository apiLogRepository;

    private final ApiResponseExampleRepository apiResponseExampleRepository;

    private final String DEFAULT_MOCK_RESULT = "docdocNoMapping";

    private final String SPLIT = "@_@";

    @Value("${owner.url.cache}")
    private boolean ownerUrlCacheEnabled;

    private LoadingCache<String, String> ownerUrlCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .refreshAfterWrite(10, TimeUnit.SECONDS)
            .build(new CacheLoader<String, String>() {
                @Override
                @ParametersAreNonnullByDefault
                public String load(String keyParam) {
                    String[] keys = keyParam.split(SPLIT);
                    if (keys.length == 3) {
                        return mockResponse(new MockUrl(keys[0], keys[1], Boolean.valueOf(keys[2])));
                    } else {
                        return DEFAULT_MOCK_RESULT;
                    }
                }
            });

    @Autowired
    public DocServiceImpl(ProjectRepository projectRepository,
                          ApiRepository apiRepository,
                          ApiFieldRepository apiFieldRepository,
                          ApiLogRepository apiLogRepository,
                          ApiResponseExampleRepository apiResponseExampleRepository) {
        this.projectRepository = projectRepository;
        this.apiRepository = apiRepository;
        this.apiFieldRepository = apiFieldRepository;
        this.apiLogRepository = apiLogRepository;
        this.apiResponseExampleRepository = apiResponseExampleRepository;
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
    public JsonObject projectEdit(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        String name = jsonObject.getString("name");
        Optional<Project> fProject = projectRepository.findById(id);
        if (fProject.isPresent()) {
            Project project = fProject.get();
            project.setName(name);
            projectRepository.save(project);
            return Result.ofSuccess();
        } else {
            throw new RuntimeException("项目不存在");
        }
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
        String regexUrl = requestUrl.replaceAll("(\\{[^}]+})", "([^/]+)");
        api.setRegexUrl(StringUtils.equals(regexUrl, requestUrl) ? "" : regexUrl);
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
    public JsonObject apiList(JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        String status = jsonObject.getString("status");
        Long projectId = Long.valueOf(jsonObject.getValue("projectId").toString());

        Api sApi = new Api();
        sApi.setIsActivity(1);
        sApi.setProjectId(projectId);
        sApi.setVersion(null);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching();

        if (StringUtils.isNotBlank(name)) {
            sApi.setName(name);
            exampleMatcher = exampleMatcher.withMatcher("name", ExampleMatcher.GenericPropertyMatchers.startsWith());
        }

        if (StringUtils.isNotBlank(status)) {
            sApi.setStatus(status);
        }

        List<Api> list = apiRepository.findAll(Example.of(sApi, exampleMatcher), Sort.by(Sort.Direction.DESC, "id"));

        return Result.ofList(list);
    }

    @Override
    public JsonObject apiEdit(JsonObject jsonObject) {
        Long id = Long.valueOf(jsonObject.getValue("id").toString());
        return Result.ofData(jsonObject.mergeIn(JsonObject.mapFrom(apiOne(id))));
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
            api.setUpdateTime(new Date());
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
            api.setUpdateTime(new Date());
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
                    list = apiFieldRepository.findByVersionLessThanAndApiIdAndActionTypeAndIsActivityOrderByVersionDesc(curVersion, curApiField.getApiId(), curApiField.getActionType(), 1);
                    break;
                case "down":
                    list = apiFieldRepository.findByVersionGreaterThanAndApiIdAndActionTypeAndIsActivityOrderByVersionAsc(curVersion, curApiField.getApiId(), curApiField.getActionType(), 1);
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
        List<ApiField> list = apiFieldRepository.findAll(
                Example.of(sApiField, ExampleMatcher.matching()),
                Sort.by(Sort.Direction.ASC, "version")
        );
        return Result.ofList(list);
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
        List<ApiLog> list = apiLogRepository.findAll(
                Example.of(sApiLog, ExampleMatcher.matching()),
                Sort.by(Sort.Direction.ASC, "id")
        );
        return Result.ofList(list);
    }

    @Override
    public JsonObject apiResponseExampleAdd(JsonObject jsonObject) {
        Long apiId = Long.valueOf(jsonObject.getValue("apiId", "-1").toString());
        String owner = jsonObject.getString("owner");
        String responseExample = jsonObject.getString("responseExample");
        ApiResponseExample apiResponseExample;
        Optional<ApiResponseExample> fApiResponseExample = findApiResponseExample(apiId, owner);
        if (fApiResponseExample.isPresent()) {
            apiResponseExample = fApiResponseExample.get();
        } else {
            apiResponseExample = new ApiResponseExample();
            apiResponseExample.setApiId(apiId);
            apiResponseExample.setOwner(owner);
        }
        apiResponseExample.setResponseExample(responseExample);
        apiResponseExample.setUpdateTime(new Date());
        apiResponseExample = apiResponseExampleRepository.save(apiResponseExample);
        return Result.ofData(apiResponseExample.getId());
    }

    @Override
    public JsonObject apiResponseExampleDel(JsonObject jsonObject) {
        try {
            Long apiId = Long.valueOf(jsonObject.getValue("apiId").toString());
            String owner = jsonObject.getString("owner");
            Optional<ApiResponseExample> fApiResponseExample = findApiResponseExample(apiId, owner);
            fApiResponseExample.ifPresent(apiResponseExampleRepository::delete);
            return Result.ofSuccess();
        } catch (Exception e) {
            throw new RuntimeException("API文档返回样例不存在");
        }
    }

    @Override
    public JsonObject apiResponseExampleOne(JsonObject jsonObject) {
        Long apiId = Long.valueOf(jsonObject.getValue("apiId").toString());
        String owner = jsonObject.getString("owner");
        Optional<ApiResponseExample> fApiResponseExample = findApiResponseExample(apiId, owner);
        Map<String, Object> result = new HashMap<>();
        if (fApiResponseExample.isPresent()) {
            result.put("type", "custom");
            result.put("owner", fApiResponseExample.get().getOwner());
            result.put("response", fApiResponseExample.get().getResponseExample());
        } else {
            result.put("type", "default");
        }
        return Result.ofData(result);
    }

    private Optional<ApiResponseExample> findApiResponseExample(Long apiId, String owner) {
        ApiResponseExample sApiResponseExample = new ApiResponseExample();
        sApiResponseExample.setApiId(apiId);
        sApiResponseExample.setOwner(owner);
        Example<ApiResponseExample> apiResponseExampleExample = Example.of(sApiResponseExample, ExampleMatcher.matching());
        return apiResponseExampleRepository.findOne(apiResponseExampleExample);
    }

    @Override
    public JsonObject apiMock(JsonObject jsonObject) {
        MockUrl mockUrl = jsonObject.mapTo(MockUrl.class);
        return Result.ofData(mockResult(mockUrl));
    }

    @Override
    public JsonObject helperLocations(JsonObject jsonObject) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> devLocations = new ArrayList<>();
        List<Api> latestApi = apiRepository.findAll(
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "updateTime"))
        ).getContent();
        if (latestApi.size() > 0) {
            result.put("docTimeLatest", latestApi.get(0).getUpdateTime());
        } else {
            result.put("docTimeLatest", "");
        }
        Api sApi = new Api();
        sApi.setVersion(null);
        sApi.setIsActivity(1);
        sApi.setStatus(Api.STATUS_DEVELOP);
        List<Api> list = apiRepository.findAll(Example.of(sApi, ExampleMatcher.matching()));
        if (list.size() > 0) {
            for (Api api : list) {
                Map<String, Object> devLocation = new HashMap<>();
                devLocation.put("url", StringUtils.defaultIfBlank(api.getRegexUrl(), api.getRequestUrl()));
                devLocation.put("restful", StringUtils.isNotBlank(api.getRegexUrl()));
                devLocations.add(devLocation);
            }
        }
        result.put("devLocations", devLocations);
        return Result.ofData(result);
    }

    private String mockResult(MockUrl mockUrl) {
        String keyParam = mockUrl.getUrl() + SPLIT + mockUrl.getOwner() + SPLIT + mockUrl.isRestful();
        String mockResult = DEFAULT_MOCK_RESULT;
        if (ownerUrlCacheEnabled) {
            try {
                mockResult = ownerUrlCache.get(keyParam);
            } catch (ExecutionException e) {
                LOGGER.error(BaseUtil.getExceptionStackTrace(e));
            }
        } else {
            mockResult = mockResponse(mockUrl);
        }
        return mockResult;
    }

    private String mockResponse(MockUrl mockUrl) {
        if (mockUrl.isRestful()) {
            return mockResponseRestful(mockUrl);
        } else {
            return mockResponseExact(mockUrl);
        }
    }

    private String mockResponseRestful(MockUrl mockUrl) {
        Api sApi = new Api();
        sApi.setStatus(Api.STATUS_DEVELOP);
        sApi.setIsActivity(1);
        sApi.setVersion(null);
        List<Api> list = apiRepository.findAll(Example.of(sApi, ExampleMatcher.matching()));
        for (Api api : list) {
            if (mockUrl.getUrl().matches(api.getRegexUrl())) {
                return mockResponseFind(api, mockUrl.getOwner());
            }
        }
        return DEFAULT_MOCK_RESULT;
    }

    private String mockResponseExact(MockUrl mockUrl) {
        String mockResult = DEFAULT_MOCK_RESULT;
        Api sApi = new Api();
        sApi.setRequestUrl(mockUrl.getUrl());
        sApi.setStatus(Api.STATUS_DEVELOP);
        sApi.setIsActivity(1);
        sApi.setVersion(null);
        Optional<Api> fApi = apiRepository.findOne(Example.of(sApi, ExampleMatcher.matching()));
        if (fApi.isPresent()) {
            mockResult = mockResponseFind(fApi.get(), mockUrl.getOwner());
        }
        return mockResult;
    }

    private String mockResponseFind(Api api, String owner) {
        String mockResult;
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("apiId", api.getId());
        jsonObject.put("owner", owner);
        JsonObject result = apiResponseExampleOne(jsonObject);
        Map<String, Object> dataMap = ((JsonObject) result.getValue("data")).getMap();
        if (dataMap.get("type").toString().equals("custom")) {
            mockResult = dataMap.get("response") != null ? dataMap.get("response").toString() : "请在【响应模板】【设置自定义模板】";
        } else {
            mockResult = StringUtils.defaultIfBlank(api.getResponseExample(), "请在【响应模板】【设置默认模板】");
        }
        return mockResult;
    }
}
