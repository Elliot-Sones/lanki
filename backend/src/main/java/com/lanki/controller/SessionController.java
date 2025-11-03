package com.lanki.controller;

import com.lanki.dto.SessionDTO;
import com.lanki.model.User;
import com.lanki.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles problem-solving session lifecycle.
 *
 * Flow:
 * 1. Frontend calls POST /api/sessions/start when user clicks "Start Problem"
 * 2. Frontend polls GET /api/sessions/{id}/check every 5 seconds
 * 3. When submission detected, returns session with completion data
 */
@Slf4j
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SessionController {

    private final SessionService sessionService;

    /**
     * POST /api/sessions/start
     * Starts a new problem session.
     * Called when user confirms "Yes, start problem" in modal.
     */
    @PostMapping("/start")
    public ResponseEntity<SessionDTO> startSession(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, Long> request) {

        Long problemId = request.get("problemId");
        if (problemId == null) {
            return ResponseEntity.badRequest().build();
        }

        SessionDTO session = sessionService.startSession(user, problemId);
        log.info("Started session {} for problem {}", session.getId(), problemId);
        return ResponseEntity.ok(session);
    }

    /**
     * GET /api/sessions/{id}/check
     * Checks if a submission has been detected for this session.
     * Frontend polls this every 5 seconds while session is active.
     *
     * Returns session with updated status:
     * - If ACTIVE: no submission yet, keep polling
     * - If COMPLETED: submission detected! Return submission data + elapsed time
     */
    @GetMapping("/{id}/check")
    public ResponseEntity<SessionDTO> checkSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        SessionDTO session = sessionService.checkForSubmission(user, id);
        return ResponseEntity.ok(session);
    }

    /**
     * GET /api/sessions/{id}
     * Get session details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SessionDTO> getSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        SessionDTO session = sessionService.getSession(user, id);
        return ResponseEntity.ok(session);
    }

    /**
     * GET /api/sessions/active
     * Get all active sessions for the current user.
     */
    @GetMapping("/active")
    public ResponseEntity<List<SessionDTO>> getActiveSessions(
            @AuthenticationPrincipal User user) {

        List<SessionDTO> sessions = sessionService.getActiveSessions(user);
        return ResponseEntity.ok(sessions);
    }

    /**
     * POST /api/sessions/{id}/abandon
     * Marks a session as abandoned (user gave up or closed tab).
     */
    @PostMapping("/{id}/abandon")
    public ResponseEntity<Void> abandonSession(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {

        sessionService.abandonSession(user, id);
        return ResponseEntity.ok().build();
    }
}
