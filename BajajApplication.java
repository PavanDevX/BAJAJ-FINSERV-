package com.example.bajaj;

import com.example.bajaj.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootApplication
public class BajajApplication implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(BajajApplication.class, args);
    }

    @Override
    public void run(String... args) {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook";

        UserInfo user = new UserInfo("John Doe", "REG12347", "john@example.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserInfo> entity = new HttpEntity<>(user, headers);

        try {
            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(url, entity, WebhookResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                WebhookResponse body = response.getBody();
                List<List<Integer>> outcome = solveMutualFollowers(body.getData().getUsers());

                ResultQ1 result = new ResultQ1(user.getRegNo(), outcome);

                sendToWebhook(body.getWebhook(), body.getAccessToken(), result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<List<Integer>> solveMutualFollowers(List<User> users) {
        Map<Integer, Set<Integer>> followMap = new HashMap<>();
        for (User user : users) {
            followMap.put(user.getId(), new HashSet<>(user.getFollows()));
        }

        Set<String> seen = new HashSet<>();
        List<List<Integer>> result = new ArrayList<>();

        for (User user : users) {
            int userId = user.getId();
            for (int followedId : user.getFollows()) {
                if (followMap.containsKey(followedId) && followMap.get(followedId).contains(userId)) {
                    int min = Math.min(userId, followedId);
                    int max = Math.max(userId, followedId);
                    String key = min + "-" + max;
                    if (!seen.contains(key)) {
                        result.add(Arrays.asList(min, max));
                        seen.add(key);
                    }
                }
            }
        }
        return result;
    }

    private void sendToWebhook(String webhook, String token, ResultQ1 result) {
        int attempts = 4;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        HttpEntity<ResultQ1> request = new HttpEntity<>(result, headers);

        while (attempts-- > 0) {
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(webhook, request, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("Success: Data sent to webhook.");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Webhook attempt failed. Retries left: " + attempts);
            }
        }

        System.err.println("All retries failed. Webhook delivery unsuccessful.");
    }
}
