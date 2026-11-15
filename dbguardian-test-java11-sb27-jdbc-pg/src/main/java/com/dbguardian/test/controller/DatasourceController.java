package com.dbguardian.test.controller;

import io.dbguardian.spring.coordination.DatasourceCoordinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/datasource")
public class DatasourceController {

    @Autowired(required = false)
    private DatasourceCoordinationService coordinationService;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        if (coordinationService != null) {
            result.put("coordinationAvailable", true);
            result.put("coordinationStatus", coordinationService.getCoordinationStatus());
        } else {
            result.put("coordinationAvailable", false);
            result.put("message", "协调服务不可用");
        }
        return result;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "dbguardian-test-java11-sb27-jdbc-pg");
        return result;
    }
}
