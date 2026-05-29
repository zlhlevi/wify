package com.wify.provider.controller;

import com.wify.common.dto.PageResult;
import com.wify.common.dto.Result;
import com.wify.provider.dto.ConnectionTestResult;
import com.wify.provider.dto.ProviderCreateReq;
import com.wify.provider.dto.ProviderDetailResp;
import com.wify.provider.dto.ProviderResp;
import com.wify.provider.dto.ProviderUpdateReq;
import com.wify.provider.service.ProviderConnectionTestService;
import com.wify.provider.service.ProviderService;
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
@RequestMapping("/api/v1/providers")
public class ProviderController {

    private final ProviderService providerService;
    private final ProviderConnectionTestService providerConnectionTestService;

    public ProviderController(
            ProviderService providerService,
            ProviderConnectionTestService providerConnectionTestService) {
        this.providerService = providerService;
        this.providerConnectionTestService = providerConnectionTestService;
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody ProviderCreateReq req) {
        return Result.ok(providerService.create(req));
    }

    @GetMapping("/{id}")
    public Result<ProviderDetailResp> getById(@PathVariable("id") Long id) {
        return Result.ok(providerService.getById(id));
    }

    @GetMapping
    public Result<PageResult<ProviderResp>> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "enabled", required = false) Integer enabled) {
        return Result.ok(providerService.list(page, pageSize, type, enabled));
    }

    @PutMapping("/{id}")
    public Result<Long> update(@PathVariable("id") Long id, @Valid @RequestBody ProviderUpdateReq req) {
        return Result.ok(providerService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        providerService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/test-connection")
    public Result<ConnectionTestResult> testConnection(@PathVariable("id") Long id) {
        return Result.ok(providerConnectionTestService.testConnection(id));
    }
}
