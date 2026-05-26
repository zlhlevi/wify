package com.wify.app.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wify.app.demo.dto.DemoItemCreateReq;
import com.wify.app.demo.dto.DemoItemResp;
import com.wify.app.demo.dto.DemoItemUpdateReq;
import com.wify.app.demo.entity.DemoItem;
import com.wify.app.demo.mapper.DemoItemMapper;
import com.wify.app.demo.service.DemoItemService;
import com.wify.common.constant.ErrorCode;
import com.wify.common.dto.PageResult;
import com.wify.common.exception.BizException;
import com.wify.common.util.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoItemServiceImpl implements DemoItemService {

    private final DemoItemMapper demoItemMapper;

    public DemoItemServiceImpl(DemoItemMapper demoItemMapper) {
        this.demoItemMapper = demoItemMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DemoItemCreateReq req) {
        DemoItem demoItem = new DemoItem();
        demoItem.setName(req.getName());
        demoItem.setStatus(req.getStatus());
        demoItemMapper.insert(demoItem);
        return demoItem.getId();
    }

    @Override
    public DemoItemResp getById(Long id) {
        return toResp(getEntity(id));
    }

    @Override
    public PageResult<DemoItemResp> list(Integer page, Integer pageSize) {
        LambdaQueryWrapper<DemoItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(DemoItem::getId);
        IPage<DemoItemResp> result = demoItemMapper
                .selectPage(PageHelper.toPage(page, pageSize), queryWrapper)
                .convert(this::toResp);
        return PageHelper.toPageResult(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long update(Long id, DemoItemUpdateReq req) {
        DemoItem demoItem = getEntity(id);
        demoItem.setName(req.getName());
        demoItem.setStatus(req.getStatus());
        demoItemMapper.updateById(demoItem);
        return demoItem.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DemoItem demoItem = getEntity(id);
        demoItemMapper.deleteById(demoItem.getId());
    }

    private DemoItem getEntity(Long id) {
        DemoItem demoItem = demoItemMapper.selectById(id);
        if (demoItem == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "demo item not found");
        }
        return demoItem;
    }

    private DemoItemResp toResp(DemoItem demoItem) {
        DemoItemResp resp = new DemoItemResp();
        resp.setId(demoItem.getId());
        resp.setName(demoItem.getName());
        resp.setStatus(demoItem.getStatus());
        resp.setCreatedAt(demoItem.getCreatedAt());
        resp.setUpdatedAt(demoItem.getUpdatedAt());
        return resp;
    }
}
