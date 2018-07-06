package xyz.fz.docdoc.service.impl;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import xyz.fz.docdoc.entity.User;
import xyz.fz.docdoc.model.Result;
import xyz.fz.docdoc.repository.UserRepository;
import xyz.fz.docdoc.service.UserService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(rollbackOn = Exception.class)
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public JsonObject add(JsonObject jsonObject) {
        String userName = jsonObject.getString("userName");
        String passWord = jsonObject.getString("passWord");
        User sUser = new User();
        sUser.setUserName(userName);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("userName", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<User> userExample = Example.of(sUser, matcher);
        Optional<User> fUser = userRepository.findOne(userExample);
        if (fUser.isPresent()) {
            throw new RuntimeException("当前用户名已存在");
        } else {
            User user = new User();
            user.setUserName(userName);
            user.setPassWord(passWord);
            userRepository.save(user);
            return Result.ofSuccess();
        }
    }

    @Override
    public JsonObject login(JsonObject jsonObject) {
        String userName = jsonObject.getString("userName");
        String passWord = jsonObject.getString("passWord");
        User sUser = new User();
        sUser.setUserName(userName);
        sUser.setPassWord(passWord);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("userName", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("passWord", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<User> userExample = Example.of(sUser, matcher);
        Optional<User> fUser = userRepository.findOne(userExample);
        if (!fUser.isPresent()) {
            throw new RuntimeException("账号密码错误");
        }
        User user = fUser.get();
        return JsonObject.mapFrom(user);
    }

    @Override
    public JsonObject list(JsonObject jsonObject) {
        LOGGER.debug("user list params: {}", jsonObject.toString());
        Sort sort = new Sort(Sort.Direction.ASC, "id");
        List<User> list = userRepository.findByUserNameNot("admin", sort);
        JsonObject result = new JsonObject();
        result.put("code", 0);
        result.put("msg", "");
        result.put("data", list);
        result.put("count", list.size());
        return result;
    }

    @Override
    public JsonObject del(JsonObject jsonObject) {
        LOGGER.debug("user del params: {}", jsonObject.toString());
        Optional<User> fUser = userRepository.findById(jsonObject.getLong("id"));
        userRepository.deleteById(jsonObject.getLong("id"));
        if (fUser.isPresent()) {
            return Result.ofData(fUser.get().getUserName());
        } else {
            return Result.ofMessage("没有找到该用户");
        }
    }

    @Override
    public JsonObject adminUpdate(JsonObject jsonObject) {
        String adminOldPassWord = jsonObject.getString("adminOldPassWord");
        String adminNewPassWord = jsonObject.getString("adminNewPassWord");
        User sUser = new User();
        sUser.setUserName("admin");
        sUser.setPassWord(adminOldPassWord);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("userName", ExampleMatcher.GenericPropertyMatchers.exact())
                .withMatcher("passWord", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<User> userExample = Example.of(sUser, exampleMatcher);
        Optional<User> fUser = userRepository.findOne(userExample);
        if (fUser.isPresent()) {
            User user = fUser.get();
            user.setPassWord(adminNewPassWord);
            userRepository.save(user);
            return Result.ofSuccess();
        } else {
            throw new RuntimeException("原始密码不正确");
        }
    }
}
