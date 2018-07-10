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
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import xyz.fz.docdoc.entity.Api;
import xyz.fz.docdoc.entity.Project;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.repository.ApiRepository;
import xyz.fz.docdoc.repository.ProjectRepository;
import xyz.fz.docdoc.service.DocService;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional(rollbackOn = Exception.class)
public class DocServiceImpl implements DocService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocServiceImpl.class);

    private final ProjectRepository projectRepository;

    private final ApiRepository apiRepository;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DocServiceImpl(ProjectRepository projectRepository, ApiRepository apiRepository, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.projectRepository = projectRepository;
        this.apiRepository = apiRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
    public JsonObject apiList(JsonObject jsonObject) {
        String name = jsonObject.getString("name");
        String status = jsonObject.getString("status");
        Map<String, Object> params = new HashMap<>();
        String sql = "";
        sql += "select d.id as id, d.name as name, d.requestUrl as requestUrl, d.status as status, d.updateTime as updateTime from t_doc_api d where 1=1 and d.isActivity = 1 ";
        if (StringUtils.isNotBlank(name)) {
            sql += "and d.name like concat(:name, '%') ";
            params.put("name", name);
        }
        if (StringUtils.isNotBlank(status)) {
            sql += "and d.status = :status ";
            params.put("status", status);
        }
        List<Map<String, Object>> list = namedParameterJdbcTemplate.queryForList(sql, params);
        if (list.size() > 0) {
            for (Map<String, Object> data : list) {
                data.put("id", data.get("id"));
                data.put("name", data.get("name"));
                data.put("requestUrl", data.get("requestUrl"));
                data.put("status", data.get("status"));
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
        Optional<Api> fApi = apiRepository.findById(id);
        if (fApi.isPresent()) {
            return Result.ofData(JsonObject.mapFrom(fApi.get()));
        } else {
            throw new RuntimeException("API文档不存在");
        }
    }
}
