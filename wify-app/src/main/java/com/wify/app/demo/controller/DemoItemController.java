package com.wify.app.demo.controller;

import com.wify.app.demo.dto.DemoItemCreateReq;
import com.wify.app.demo.dto.DemoItemResp;
import com.wify.app.demo.dto.DemoItemUpdateReq;
import com.wify.app.demo.service.DemoItemService;
import com.wify.common.dto.PageResult;
import com.wify.common.dto.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo-items")
public class DemoItemController {

    private final DemoItemService demoItemService;

    public DemoItemController(DemoItemService demoItemService) {
        this.demoItemService = demoItemService;
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody DemoItemCreateReq req) {
        return Result.ok(demoItemService.create(req));
    }

    @GetMapping("/{id}")
    public Result<DemoItemResp> getById(@PathVariable("id") Long id) {
        return Result.ok(demoItemService.getById(id));
    }

    @GetMapping
    public Result<PageResult<DemoItemResp>> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return Result.ok(demoItemService.list(page, pageSize));
    }

    @PutMapping("/{id}")
    public Result<Long> update(@PathVariable("id") Long id, @Valid @RequestBody DemoItemUpdateReq req) {
        return Result.ok(demoItemService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        demoItemService.delete(id);
        return Result.ok();
    }
}
