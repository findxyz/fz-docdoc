package xyz.fz.docdoc.service.impl;

import io.vertx.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import xyz.fz.docdoc.entity.User;
import xyz.fz.docdoc.repository.UserRepository;
import xyz.fz.docdoc.service.UserService;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public JsonObject add(JsonObject jsonObject) {
        String userName = jsonObject.getString("userName");
        String passWord = jsonObject.getString("passWord");
        ExampleMatcher matcher = ExampleMatcher.matching();
        User sUser = new User();
        sUser.setUserName(userName);
        matcher = matcher.withMatcher("userName", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<User> userExample = Example.of(sUser, matcher);
        Optional<User> fUser = userRepository.findOne(userExample);
        if (fUser.isPresent()) {
            throw new RuntimeException("当前用户名已存在");
        } else {
            User user = new User();
            user.setUserName(userName);
            user.setPassWord(passWord);
            userRepository.save(user);
            return new JsonObject();
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
        return new JsonObject().put("id", user.getId()).put("userName", user.getUserName()).put("passWord", user.getPassWord());
    }
}
