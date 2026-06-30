package com.wify.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wify.common.dto.PageResult;

public final class PageHelper {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 20L;
    private static final long MAX_PAGE_SIZE = 100L;

    private PageHelper() {
    }

    public static <T> Page<T> toPage(Number page, Number pageSize) {
        long current = page == null ? DEFAULT_PAGE : Math.max(page.longValue(), DEFAULT_PAGE);
        long size = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize.longValue();
        if (size <= 0) {
            size = DEFAULT_PAGE_SIZE;
        }
        size = Math.min(size, MAX_PAGE_SIZE);
        return new Page<>(current, size);
    }

    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        if (page == null) {
            return PageResult.of(null, 0L, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
        }
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
}
