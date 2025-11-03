package com.lanki.controller;

import com.lanki.model.User;
import com.lanki.service.LeetCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final LeetCodeService leetCodeService;

    /**
     * GET /api/user/profile
     * Returns current user's profile information.
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "leetcodeUsername", user.getLeetcodeUsername() != null ? user.getLeetcodeUsername() : "",
            "hasLeetcodeCredentials", user.getLeetcodeSession() != null
        ));
    }

    /**
     * POST /api/user/leetcode-credentials
     * Saves user's LeetCode credentials for submission tracking.
     */
    @PostMapping("/leetcode-credentials")
    public ResponseEntity<Map<String, Object>> saveLeetCodeCredentials(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> request) {

        String sessionToken = request.get("sessionToken");
        String csrfToken = request.get("csrfToken");
        String leetcodeUsername = request.get("username");

        // Validate credentials
        boolean valid = leetCodeService.validateCredentials(sessionToken, csrfToken);

        if (!valid) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid LeetCode credentials"));
        }

        // Update user (in real implementation, encrypt the session token)
        user.setLeetcodeSession(sessionToken);
        user.setCsrfToken(csrfToken);
        user.setLeetcodeUsername(leetcodeUsername);
        // Save user to repository...

        return ResponseEntity.ok(Map.of("success", true));
    }
}
