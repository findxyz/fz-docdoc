package xyz.fz.docdoc.service.impl;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import xyz.fz.docdoc.entity.Project;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.repository.ProjectRepository;
import xyz.fz.docdoc.service.DocService;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
public class DocServiceImpl implements DocService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocServiceImpl.class);

    private final ProjectRepository projectRepository;

    @Autowired
    public DocServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
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
}
