package xyz.fz.docdoc.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by fz on 2015/11/7.
 */
public interface CommonDao {

    /* hql相关查询 */

    <T> List<T> queryList(String hql, Map<String, Object> params);

    <T> PagerData<T> queryPagerData(String countHql, String hql, Map<String, Object> params, int currentPage, int pageSize);

    <T> T querySingle(String hql, Map<String, Object> params);

    <T> T findById(Class<T> clazz, Object id);

    /* sql相关查询 */

    <T> List<T> queryListBySql(String sql, Map<String, Object> params, Class<T> clazz);

    <T> PagerData<T> queryPagerDataBySql(String countSql, String sql, Map<String, Object> params, int currentPage, int pageSize, Class<T> clazz);

    <T> T querySingleBySql(String sql, Map<String, Object> params, Class<T> clazz);

    /* 非查询dao方法 */

    void save(Object entity);

    void batchSave(Collection entities);

    void update(Object entity);

    void delete(Object entity);

    int execute(String hql, Map<String, Object> params);

    int executeBySql(String sql, Map<String, Object> params);
}
