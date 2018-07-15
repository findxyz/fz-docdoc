package xyz.fz.docdoc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.fz.docdoc.entity.ApiField;

public interface ApiFieldRepository extends JpaRepository<ApiField, Long> {
}
