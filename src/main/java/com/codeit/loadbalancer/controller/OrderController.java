package com.codeit.loadbalancer.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // 메모리 기반 주문 데이터
    private static final List<Map<String, Object>> orders = new ArrayList<>();

    static {
        // 초기 데이터
        Map<String, Object> order1 = new HashMap<>();
        order1.put("id", 1);
        order1.put("userId", 1);
        order1.put("productName", "노트북");
        order1.put("quantity", 1);
        order1.put("price", 1500000);
        order1.put("status", "배송중");
        order1.put("orderDate", "2025-01-01T10:00:00");
        orders.add(order1);

        Map<String, Object> order2 = new HashMap<>();
        order2.put("id", 2);
        order2.put("userId", 2);
        order2.put("productName", "마우스");
        order2.put("quantity", 2);
        order2.put("price", 50000);
        order2.put("status", "주문완료");
        order2.put("orderDate", "2025-01-01T11:30:00");
        orders.add(order2);
    }

    @GetMapping
    public Map<String, Object> getAllOrders() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "주문 목록 조회 성공 (실시간 데이터)");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", orders);
        response.put("cache_info", "이 응답은 캐싱되지 않습니다 - 실시간 데이터");
        response.put("real_time", true);

        return response;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrderById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> order = orders.stream()
                .filter(o -> o.get("id").equals(id.intValue()))
                .findFirst()
                .orElse(null);

        if (order != null) {
            response.put("status", "success");
            response.put("message", "주문 조회 성공");
            response.put("data", order);
        } else {
            response.put("status", "error");
            response.put("message", "주문을 찾을 수 없습니다");
            response.put("data", null);
        }

        response.put("timestamp", LocalDateTime.now());
        response.put("cache_info", "이 응답은 캐싱되지 않습니다 - 실시간 데이터");
        response.put("real_time", true);

        return response;
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> orderData) {
        Map<String, Object> response = new HashMap<>();

        // 새 주문 ID 생성
        int newId = orders.size() + 1;
        orderData.put("id", newId);
        orderData.put("orderDate", LocalDateTime.now().toString());
        orderData.put("status", "주문완료");
        orders.add(orderData);

        response.put("status", "success");
        response.put("message", "주문 생성 성공");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", orderData);
        response.put("real_time", true);

        return response;
    }

    @PutMapping("/{id}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> order = orders.stream()
                .filter(o -> o.get("id").equals(id.intValue()))
                .findFirst()
                .orElse(null);

        if (order != null) {
            order.put("status", statusData.get("status"));
            response.put("status", "success");
            response.put("message", "주문 상태 업데이트 성공");
            response.put("data", order);
        } else {
            response.put("status", "error");
            response.put("message", "주문을 찾을 수 없습니다");
            response.put("data", null);
        }

        response.put("timestamp", LocalDateTime.now());
        response.put("real_time", true);

        return response;
    }
}
