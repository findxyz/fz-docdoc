package xyz.fz.docdoc.dao;

import java.util.List;

public class PagerData<T> {

    private static final String LOAD_STATUS_Y = "y";

    private static final String LOAD_STATUS_N = "n";

    private List<T> data;

    private Long totalCount;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    private Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public String getLoadStatus(int page, int pageSize) {
        return getTotalCount() > page * pageSize ? LOAD_STATUS_Y : LOAD_STATUS_N;
    }
}

