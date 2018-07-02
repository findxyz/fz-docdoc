package xyz.fz.docdoc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.fz.docdoc.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
