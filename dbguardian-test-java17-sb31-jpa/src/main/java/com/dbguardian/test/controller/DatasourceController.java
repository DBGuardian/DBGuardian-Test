package com.dbguardian.test.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/datasource")
public class DatasourceController {

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "success", true,
                "data", "DBGuardian 读写分离协调服务运行中",
                "message", "OK"
        );
    }
}