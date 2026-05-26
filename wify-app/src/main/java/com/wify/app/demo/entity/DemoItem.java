package com.wify.app.demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.wify.common.entity.BaseEntity;

@TableName("demo_item")
public class DemoItem extends BaseEntity {

    private String name;
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
