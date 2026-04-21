package com.chenzhang.thesis.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页响应体（对应 CLAUDE.md §5.1）
 */
@Data
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long total;
    private Integer pages;
    private Integer current;
    private Integer size;
    private List<T> records;

    public static <T> PageResult<T> empty() {
        PageResult<T> r = new PageResult<>();
        r.setTotal(0L);
        r.setPages(0);
        r.setCurrent(1);
        r.setSize(20);
        r.setRecords(Collections.emptyList());
        return r;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(page.getTotal());
        r.setPages((int) page.getPages());
        r.setCurrent((int) page.getCurrent());
        r.setSize((int) page.getSize());
        r.setRecords(page.getRecords());
        return r;
    }

    public static <T, R> PageResult<R> of(IPage<T> page, Function<T, R> converter) {
        PageResult<R> r = new PageResult<>();
        r.setTotal(page.getTotal());
        r.setPages((int) page.getPages());
        r.setCurrent((int) page.getCurrent());
        r.setSize((int) page.getSize());
        r.setRecords(page.getRecords().stream().map(converter).collect(Collectors.toList()));
        return r;
    }
}
