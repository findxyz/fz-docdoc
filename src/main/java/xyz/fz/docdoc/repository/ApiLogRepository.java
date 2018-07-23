package xyz.fz.docdoc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.fz.docdoc.entity.ApiLog;

public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {
}
