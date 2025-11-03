package com.lanki.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanki.model.Problem;
import com.lanki.model.Submission;
import com.lanki.model.User;
import com.lanki.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Integrates with LeetCode GraphQL API to check for submissions.
 * Adapted from the Python script logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeetCodeService {

    private static final String LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";

    private final SubmissionRepository submissionRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // GraphQL query to fetch recent submissions
    private static final String LATEST_SUBMISSION_QUERY = """
        query latestAcSubmission($username: String!, $limit: Int!) {
          recentAcSubmissionList(username: $username, limit: $limit) {
            id
            title
            titleSlug
            timestamp
            statusDisplay
            lang
          }
        }
        """;

    /**
     * Checks for a new submission on LeetCode for the given problem after the session started.
     * Returns null if no submission found, or Submission entity if detected.
     */
    public Submission checkForNewSubmission(User user, Problem problem, LocalDateTime sessionStartTime) {
        if (user.getLeetcodeSession() == null || user.getLeetcodeUsername() == null) {
            log.warn("User {} does not have LeetCode credentials configured", user.getId());
            return null;
        }

        try {
            // Query LeetCode for recent submissions
            JsonNode submissions = queryRecentSubmissions(
                user.getLeetcodeUsername(),
                user.getLeetcodeSession(),
                user.getCsrfToken(),
                5 // Check last 5 submissions
            );

            if (submissions == null || !submissions.isArray()) {
                return null;
            }

            // Find submission matching the problem and after session start time
            for (JsonNode subNode : submissions) {
                String titleSlug = subNode.get("titleSlug").asText();
                long timestamp = subNode.get("timestamp").asLong();
                LocalDateTime submittedAt = LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(timestamp),
                    ZoneId.systemDefault()
                );

                // Check if this submission matches our problem and is after session start
                if (titleSlug.equals(problem.getTitleSlug()) &&
                    submittedAt.isAfter(sessionStartTime)) {

                    String leetcodeId = subNode.get("id").asText();

                    // Check if we've already recorded this submission
                    if (submissionRepository.findByLeetcodeSubmissionId(leetcodeId).isPresent()) {
                        continue;
                    }

                    // Create new submission record
                    Submission submission = new Submission();
                    submission.setUser(user);
                    submission.setProblem(problem);
                    submission.setLeetcodeSubmissionId(leetcodeId);
                    submission.setSubmittedAt(submittedAt);
                    submission.setStatus(subNode.get("statusDisplay").asText());
                    submission.setLanguage(subNode.get("lang").asText());
                    // Note: code, runtime, memory would require additional API calls

                    log.info("Detected new submission for problem {} by user {}",
                        problem.getTitle(), user.getUsername());

                    return submission;
                }
            }

            return null; // No matching submission found

        } catch (Exception e) {
            log.error("Error checking LeetCode submissions for user {}: {}",
                user.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Executes GraphQL query against LeetCode API.
     */
    private JsonNode queryRecentSubmissions(String username, String sessionToken,
                                           String csrfToken, int limit) {
        try {
            // Build request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Origin", "https://leetcode.com");
            headers.set("Referer", "https://leetcode.com/");
            headers.set("x-csrftoken", csrfToken != null ? csrfToken : "");
            headers.set("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
            headers.set("Cookie",
                "LEETCODE_SESSION=" + sessionToken +
                (csrfToken != null ? "; csrftoken=" + csrfToken : ""));

            // Build GraphQL request body
            Map<String, Object> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("limit", limit);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", LATEST_SUBMISSION_QUERY);
            requestBody.put("variables", variables);
            requestBody.put("operationName", "latestAcSubmission");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Execute request
            ResponseEntity<String> response = restTemplate.postForEntity(
                LEETCODE_GRAPHQL_URL,
                request,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("LeetCode API returned error: {}", response.getStatusCode());
                return null;
            }

            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode data = root.get("data");

            if (data != null && data.has("recentAcSubmissionList")) {
                return data.get("recentAcSubmissionList");
            }

            return null;

        } catch (Exception e) {
            log.error("Error querying LeetCode API: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Validates LeetCode credentials by checking user status.
     */
    public boolean validateCredentials(String sessionToken, String csrfToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-csrftoken", csrfToken != null ? csrfToken : "");
            headers.set("Cookie", "LEETCODE_SESSION=" + sessionToken +
                (csrfToken != null ? "; csrftoken=" + csrfToken : ""));

            String query = "query { userStatus { username isSignedIn } }";
            Map<String, Object> requestBody = Map.of("query", query);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                LEETCODE_GRAPHQL_URL,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode userStatus = root.path("data").path("userStatus");
                return userStatus.path("isSignedIn").asBoolean(false);
            }

            return false;
        } catch (Exception e) {
            log.error("Error validating LeetCode credentials: {}", e.getMessage());
            return false;
        }
    }
}
