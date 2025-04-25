package com.sahil.bajaj.service;

import com.sahil.bajaj.model.User;
import com.sahil.bajaj.model.WebhookResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String INIT_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

    public void startWorkflow() {
        Map<String, Object> request = Map.of(
            "name", "Shaik Sahil",
            "regNo", "REG12347",
            "email", "sahil@example.com"
        );

        ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(
            INIT_URL, request, WebhookResponse.class
        );

        WebhookResponse res = response.getBody();
        List<List<Integer>> outcome = mutualFollowers(res.getData().getUsers());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", res.getAccessToken());

        Map<String, Object> payload = Map.of(
            "regNo", "REG12347",
            "outcome", outcome
        );

        HttpEntity<Map<String, Object>> postReq = new HttpEntity<>(payload, headers);

        for (int i = 0; i < 4; i++) {
            try {
                restTemplate.postForEntity(res.getWebhook(), postReq, String.class);
                break;
            } catch (Exception e) {
                if (i == 3) e.printStackTrace();
            }
        }
    }

    private List<List<Integer>> mutualFollowers(List<User> users) {
        List<List<Integer>> result = new ArrayList<>();
        Map<Integer, Set<Integer>> map = new HashMap<>();

        for (User user : users) {
            map.put(user.getId(), new HashSet<>(user.getFollows()));
        }

        Set<String> seen = new HashSet<>();
        for (User user : users) {
            int u = user.getId();
            for (int v : user.getFollows()) {
                if (map.containsKey(v) && map.get(v).contains(u)) {
                    int min = Math.min(u, v);
                    int max = Math.max(u, v);
                    String key = min + "-" + max;
                    if (!seen.contains(key)) {
                        result.add(List.of(min, max));
                        seen.add(key);
                    }
                }
            }
        }
        return result;
    }
}