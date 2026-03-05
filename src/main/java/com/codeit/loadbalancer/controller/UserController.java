package com.codeit.loadbalancer.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // 메모리 기반 사용자 데이터
    private static final List<Map<String, Object>> users = new ArrayList<>();

    static {
        // 초기 데이터
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1);
        user1.put("name", "김철수");
        user1.put("email", "chulsoo@example.com");
        user1.put("department", "개발팀");
        users.add(user1);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", 2);
        user2.put("name", "이영희");
        user2.put("email", "younghee@example.com");
        user2.put("department", "디자인팀");
        users.add(user2);

        Map<String, Object> user3 = new HashMap<>();
        user3.put("id", 3);
        user3.put("name", "박민수");
        user3.put("email", "minsu@example.com");
        user3.put("department", "기획팀");
        users.add(user3);
    }

    @GetMapping
    public Map<String, Object> getAllUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "사용자 목록 조회 성공 (캐싱 적용)");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", users);
        response.put("cache_info", "이 응답은 Nginx에서 5분간 캐시됩니다");

        return response;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> user = users.stream()
                .filter(u -> u.get("id").equals(id.intValue()))
                .findFirst()
                .orElse(null);

        if (user != null) {
            response.put("status", "success");
            response.put("message", "사용자 조회 성공");
            response.put("data", user);
        } else {
            response.put("status", "error");
            response.put("message", "사용자를 찾을 수 없습니다");
            response.put("data", null);
        }

        response.put("timestamp", LocalDateTime.now());
        response.put("cache_info", "이 응답은 Nginx에서 5분간 캐시됩니다");

        return response;
    }

    @PostMapping
    public Map<String, Object> createUser(@RequestBody Map<String, Object> userData) {
        Map<String, Object> response = new HashMap<>();

        // 새 사용자 ID 생성
        int newId = users.size() + 1;
        userData.put("id", newId);
        users.add(userData);

        response.put("status", "success");
        response.put("message", "사용자 생성 성공");
        response.put("timestamp", LocalDateTime.now());
        response.put("data", userData);

        return response;
    }
}
