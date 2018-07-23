package xyz.fz.docdoc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.fz.docdoc.entity.ApiResponseExample;

public interface ApiResponseExampleRepository extends JpaRepository<ApiResponseExample, Long> {
}
