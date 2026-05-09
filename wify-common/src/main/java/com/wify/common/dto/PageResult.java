package com.wify.common.dto;

import java.util.Collections;
import java.util.List;

public class PageResult<T> extends Result<List<T>> {

    private long total;
    private long page;
    private long size;

    public PageResult() {
        setData(Collections.emptyList());
    }

    public PageResult(int code, String message, List<T> data, long total, long page, long size) {
        super(code, message, data == null ? Collections.emptyList() : data);
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public static <T> PageResult<T> ok(List<T> data, long total, long page, long size) {
        return new PageResult<>(200, "success", data, total, page, size);
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
