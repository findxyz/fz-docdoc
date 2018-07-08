package xyz.fz.docdoc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.fz.docdoc.entity.Api;

public interface ApiRepository extends JpaRepository<Api, Long> {
}
