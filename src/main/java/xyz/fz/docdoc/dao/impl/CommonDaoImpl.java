package xyz.fz.docdoc.dao.impl;

import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import xyz.fz.docdoc.dao.CommonDao;
import xyz.fz.docdoc.dao.PagerData;
import xyz.fz.docdoc.util.BaseUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

/**
 * CommonDaoImpl
 *
 * @author fz
 * @date 2015/11/7
 */
@Repository
@SuppressWarnings("unchecked")
public class CommonDaoImpl implements CommonDao {

    private static Logger logger = LoggerFactory.getLogger(CommonDaoImpl.class);

    private static final Set<String> MAP_CLAZZ = new HashSet<String>() {{
        add("java.util.Map");
        add("java.util.LinkedHashMap");
        add("java.util.HashMap");
    }};

    private static final Set<String> COUNT_CLAZZ = new HashSet<String>() {{
        add("java.lang.Long");
        add("java.lang.Integer");
        add("java.math.BigInteger");
        add("java.lang.Number");
    }};

    private static final String DTO = "DTO";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T> List<T> queryList(String hql, Map<String, Object> params) {
        try {
            Query query = entityManager.createQuery(hql);
            queryParamsSet(query, params);
            return query.getResultList();
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> PagerData<T> queryPagerData(String countHql, String hql, Map<String, Object> params, int curPageNum, int pageSize) {
        try {
            PagerData pagerData = new PagerData();
            Long count = querySingle(countHql, params);
            Query query = entityManager.createQuery(hql);
            queryParamsSet(query, params);
            query.setFirstResult((curPageNum - 1) * pageSize);
            query.setMaxResults(pageSize);
            pagerData.setTotalCount(count);
            pagerData.setData(query.getResultList());
            return pagerData;
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T querySingle(String hql, Map<String, Object> params) {
        try {
            Query query = entityManager.createQuery(hql);
            queryParamsSet(query, params);
            return (T) query.getSingleResult();
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T findById(Class<T> clazz, Object id) {
        try {
            Object object = entityManager.find(clazz, id);
            return (T) object;
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }


    @Override
    public <T> List<T> queryListBySql(String sql, Map<String, Object> params, Class<T> clazz) {
        try {
            Query query = getSqlQuery(sql, params, clazz);
            return query.getResultList();
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> PagerData<T> queryPagerDataBySql(String countSql, String sql, Map<String, Object> params, int curPageNum, int pageSize, Class<T> clazz) {
        try {
            sql += " limit " + ((curPageNum - 1) * pageSize) + ", " + pageSize;
            PagerData pagerData = new PagerData();
            Number count = querySingleBySql(countSql, params, Number.class);
            Query query = getSqlQuery(sql, params, clazz);
            pagerData.setData(query.getResultList());
            pagerData.setTotalCount(count.longValue());
            return pagerData;
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T querySingleBySql(String sql, Map<String, Object> params, Class<T> clazz) {
        try {
            Query query = getSqlQuery(sql, params, clazz);
            return (T) query.getSingleResult();
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    private Query getSqlQuery(String sql, Map<String, Object> params, Class clazz) {
        Query query;
        if (MAP_CLAZZ.contains(clazz.getName())) {
            query = mapQuery(sql, params);
        } else if (COUNT_CLAZZ.contains(clazz.getName()) || String.class.getName().contains(clazz.getName())) {
            query = countStringQuery(sql, params);
        } else if (clazz.getName().contains(DTO)) {
            query = beanQuery(sql, params, clazz);
        } else {
            query = clazzQuery(sql, params, clazz);
        }
        return query;
    }

    private Query mapQuery(String sql, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(sql);
        queryParamsSet(query, params);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        return query;
    }

    private Query countStringQuery(String sql, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(sql);
        queryParamsSet(query, params);
        return query;
    }

    private Query beanQuery(String sql, Map<String, Object> params, Class clazz) {
        Query query = entityManager.createNativeQuery(sql);
        queryParamsSet(query, params);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.aliasToBean(clazz));
        return query;
    }

    private Query clazzQuery(String sql, Map<String, Object> params, Class clazz) {
        Query query = entityManager.createNativeQuery(sql, clazz);
        queryParamsSet(query, params);
        return query;
    }

    private void queryParamsSet(Query query, Map<String, Object> params) {
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void save(Object entity) {
        try {
            entityManager.persist(entity);
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void batchSave(Collection entities) {
        int i = 0;
        int batchSize = 10;
        for (Object o : entities) {
            entityManager.persist(o);
            i++;
            if (i % batchSize == 0) {
                // Flush a batch of inserts and release memory.
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    @Override
    public void update(Object entity) {
        try {
            entityManager.merge(entity);
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Object entity) {
        try {
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public int execute(String hql, Map<String, Object> params) {
        try {
            Query query = entityManager.createQuery(hql);
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }
            return query.executeUpdate();
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public int executeBySql(String sql, Map<String, Object> params) {
        try {
            Query query = entityManager.createNativeQuery(sql);
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    query.setParameter(entry.getKey(), entry.getValue());
                }
            }
            return query.executeUpdate();
        } catch (Exception e) {
            logger.error(BaseUtil.getExceptionStackTrace(e));
            throw new RuntimeException(e);
        }
    }
}
