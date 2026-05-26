package com.wify.app.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wify.app.demo.entity.DemoItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemoItemMapper extends BaseMapper<DemoItem> {
}
