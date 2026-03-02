package com.ljw.playdough.modelengine.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResult<T> {

    private final long total;
    private final int page;
    private final int pageSize;
    private final List<T> list;

    private PageResult(long total, int page, int pageSize, List<T> list) {
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.list = list;
    }

    public static <T> PageResult<T> of(long total, int page, int pageSize, List<T> list) {
        return new PageResult<>(total, page, pageSize, list);
    }

    public static <T> PageResult<T> empty(int page, int pageSize) {
        return new PageResult<>(0L, page, pageSize, List.of());
    }
}
