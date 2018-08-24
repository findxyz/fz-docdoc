package xyz.fz.docdoc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import xyz.fz.docdoc.entity.ApiField;

import java.util.List;

public interface ApiFieldRepository extends JpaRepository<ApiField, Long> {
    List<ApiField> findByVersionLessThanAndActionTypeAndIsActivityOrderByVersionDesc(Long version, String actionType, Integer isActivity);

    List<ApiField> findByVersionGreaterThanAndActionTypeAndIsActivityOrderByVersionAsc(Long version, String actionType, Integer isActivity);
}
