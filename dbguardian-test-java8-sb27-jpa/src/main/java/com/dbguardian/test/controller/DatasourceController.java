package com.dbguardian.test.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据源状态监控控制器
 */
@RestController
@RequestMapping("/datasource")
@Api(tags = "数据源状态监控")
public class DatasourceController {

    @GetMapping("/status")
    @ApiOperation("获取数据源协调状态")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", "DBGuardian 读写分离协调服务运行中");
        result.put("message", "OK");
        return result;
    }
}