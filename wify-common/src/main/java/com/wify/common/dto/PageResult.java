package com.wify.common.dto;

import java.util.Collections;
import java.util.List;

public class PageResult<T> {

    private List<T> list;

    private long total;
    private long page;
    private long pageSize;

    public PageResult() {
        setList(Collections.emptyList());
    }

    public PageResult(List<T> list, long total, long page, long pageSize) {
        this.list = list == null ? Collections.emptyList() : list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(List<T> list, long total, long page, long pageSize) {
        return new PageResult<>(list, total, page, pageSize);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list == null ? Collections.emptyList() : list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
