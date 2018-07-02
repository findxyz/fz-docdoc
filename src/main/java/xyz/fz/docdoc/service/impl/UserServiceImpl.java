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

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public boolean addUser(JsonObject jsonObject) {
        String userName = jsonObject.getString("userName");
        String passWord = jsonObject.getString("passWord");
        ExampleMatcher matcher = ExampleMatcher.matching();
        User sUser = new User();
        sUser.setUserName(userName);
        matcher = matcher.withMatcher("userName", ExampleMatcher.GenericPropertyMatchers.exact());
        Example<User> userExampleExample = Example.of(sUser, matcher);
        Optional<User> fUser = userRepository.findOne(userExampleExample);
        if (fUser.isPresent()) {
            return false;
        } else {
            User user = new User();
            user.setUserName(userName);
            user.setPassWord(passWord);
            userRepository.save(user);
            return true;
        }
    }
}
