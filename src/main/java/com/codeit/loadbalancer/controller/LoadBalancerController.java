package com.codeit.loadbalancer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/lb")
public class LoadBalancerController {

    private static final AtomicLong requestCounter = new AtomicLong(0);

    @GetMapping
    public Map<String, Object> loadBalancerTest() {
        Map<String, Object> response = new HashMap<>();

        try {
            long currentRequestId = requestCounter.incrementAndGet();

            response.put("status", "success");
            response.put("message", "로드 밸런서 테스트 성공");
            response.put("requestId", currentRequestId);
            response.put("timestamp", LocalDateTime.now());
            response.put("hostname", InetAddress.getLocalHost().getHostName());
            response.put("hostAddress", InetAddress.getLocalHost().getHostAddress());
            response.put("serverPort", System.getProperty("server.port", "8080"));
            response.put("processId", ProcessHandle.current().pid());
            response.put("threadName", Thread.currentThread().getName());

            // 로드 밸런싱 관련 정보
            Map<String, Object> loadBalanceInfo = new HashMap<>();
            loadBalanceInfo.put("totalRequests", currentRequestId);
            loadBalanceInfo.put("serverIdentifier", "Instance-" + InetAddress.getLocalHost().getHostName());
            loadBalanceInfo.put("loadBalanceAlgorithm", "Round Robin (Nginx)");
            loadBalanceInfo.put("sessionAffinity", "Disabled");

            response.put("loadBalanceInfo", loadBalanceInfo);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "로드 밸런서 테스트 실패: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/status")
    public Map<String, Object> getLoadBalancerStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("status", "UP");
            response.put("message", "로드 밸런서 정상 동작");
            response.put("timestamp", LocalDateTime.now());
            response.put("hostname", InetAddress.getLocalHost().getHostName());
            response.put("totalRequests", requestCounter.get());
            response.put("serverInfo", Map.of(
                    "hostname", InetAddress.getLocalHost().getHostName(),
                    "ip", InetAddress.getLocalHost().getHostAddress(),
                    "port", System.getProperty("server.port", "8080"),
                    "pid", ProcessHandle.current().pid()
            ));

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "상태 조회 실패: " + e.getMessage());
        }

        return response;
    }

}
