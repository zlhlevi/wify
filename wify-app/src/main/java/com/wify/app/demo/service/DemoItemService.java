package com.wify.app.demo.service;

import com.wify.app.demo.dto.DemoItemCreateReq;
import com.wify.app.demo.dto.DemoItemResp;
import com.wify.app.demo.dto.DemoItemUpdateReq;
import com.wify.common.dto.PageResult;

public interface DemoItemService {

    Long create(DemoItemCreateReq req);

    DemoItemResp getById(Long id);

    PageResult<DemoItemResp> list(Integer page, Integer pageSize);

    Long update(Long id, DemoItemUpdateReq req);

    void delete(Long id);
}
