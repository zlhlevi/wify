package com.wify.common.dto;

import java.util.Collections;
import java.util.List;

public class PageResult<T> extends Result<List<T>> {

    private long total;
    private long page;
    private long pageSize;

    public PageResult() {
        setData(Collections.emptyList());
    }

    public PageResult(int code, String message, List<T> data, long total, long page, long pageSize) {
        super(code, message, data == null ? Collections.emptyList() : data);
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> ok(List<T> data, long total, long page, long pageSize) {
        return new PageResult<>(200, "success", data, total, page, pageSize);
    }

    @Override
    public void setData(List<T> data) {
        super.setData(data == null ? Collections.emptyList() : data);
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
