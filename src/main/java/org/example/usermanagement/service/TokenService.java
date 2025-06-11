package org.example.usermanagement.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {
    private final Map<String, String> userTokens = new ConcurrentHashMap<>();

    public String getActiveTokenForUser(String username) {
        return userTokens.get(username);
    }

    public void saveTokenForUser(String username, String token) {
        userTokens.put(username, token);
    }

    public void removeTokenForUser(String username) {
        userTokens.remove(username);
    }
}
