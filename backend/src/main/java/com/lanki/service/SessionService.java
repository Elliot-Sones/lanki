package com.lanki.service;

import com.lanki.dto.SessionDTO;
import com.lanki.dto.SubmissionDTO;
import com.lanki.model.*;
import com.lanki.repository.ProblemRepository;
import com.lanki.repository.ProblemSessionRepository;
import com.lanki.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages problem-solving sessions.
 * Key flow:
 * 1. User starts session → creates ProblemSession record
 * 2. Frontend polls checkForSubmission() every 5 seconds
 * 3. When submission detected → completes session with elapsed time
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final ProblemSessionRepository sessionRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final LeetCodeService leetCodeService;
    private final UserProgressService userProgressService;

    /**
     * Starts a new problem session.
     * Called when user clicks "Yes, start problem" in the modal.
     */
    @Transactional
    public SessionDTO startSession(User user, Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        // Check if there's already an active session for this problem
        List<ProblemSession> activeSessions = sessionRepository.findActiveSessionsByUserId(user.getId());
        for (ProblemSession session : activeSessions) {
            if (session.getProblem().getId().equals(problemId)) {
                log.warn("User {} already has an active session for problem {}", user.getId(), problemId);
                return toDTO(session);
            }
        }

        ProblemSession session = new ProblemSession();
        session.setUser(user);
        session.setProblem(problem);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus(ProblemSession.SessionStatus.ACTIVE);

        session = sessionRepository.save(session);

        // Update user progress
        userProgressService.markAsInProgress(user, problem);

        log.info("Started session {} for user {} on problem {}", session.getId(), user.getId(), problemId);
        return toDTO(session);
    }

    /**
     * Checks for submission on LeetCode for an active session.
     * Called by frontend every 5 seconds.
     * Returns null if no submission found, or SubmissionDTO if detected.
     */
    @Transactional
    public SessionDTO checkForSubmission(User user, Long sessionId) {
        ProblemSession session = sessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getStatus() != ProblemSession.SessionStatus.ACTIVE) {
            // Session already completed or abandoned
            return toDTO(session);
        }

        // Check LeetCode for new submission
        Submission newSubmission = leetCodeService.checkForNewSubmission(
            user,
            session.getProblem(),
            session.getStartedAt()
        );

        if (newSubmission != null) {
            // Submission detected! Complete the session
            newSubmission.setSession(session);
            newSubmission = submissionRepository.save(newSubmission);

            session.complete(newSubmission);
            sessionRepository.save(session);

            // Update user progress
            if ("Accepted".equals(newSubmission.getStatus())) {
                userProgressService.markAsCompleted(user, session.getProblem());
            }

            log.info("Session {} completed in {} seconds",
                session.getId(), session.getElapsedTimeSeconds());
        }

        return toDTO(session);
    }

    /**
     * Gets session details by ID.
     */
    @Transactional(readOnly = true)
    public SessionDTO getSession(User user, Long sessionId) {
        ProblemSession session = sessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        return toDTO(session);
    }

    /**
     * Gets all active sessions for a user.
     */
    @Transactional(readOnly = true)
    public List<SessionDTO> getActiveSessions(User user) {
        return sessionRepository.findActiveSessionsByUserId(user.getId())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Abandons a session (user gave up or closed tab).
     */
    @Transactional
    public void abandonSession(User user, Long sessionId) {
        ProblemSession session = sessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        session.setStatus(ProblemSession.SessionStatus.ABANDONED);
        session.setCompletedAt(LocalDateTime.now());
        sessionRepository.save(session);

        log.info("Session {} abandoned", sessionId);
    }

    private SessionDTO toDTO(ProblemSession session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setProblemId(session.getProblem().getId());
        dto.setProblemTitle(session.getProblem().getTitle());
        dto.setStartedAt(session.getStartedAt());
        dto.setCompletedAt(session.getCompletedAt());
        dto.setStatus(session.getStatus().name());
        dto.setElapsedTimeSeconds(session.getElapsedTimeSeconds());

        if (session.getSubmission() != null) {
            Submission sub = session.getSubmission();
            SubmissionDTO subDto = new SubmissionDTO();
            subDto.setId(sub.getId());
            subDto.setCode(sub.getCode());
            subDto.setLanguage(sub.getLanguage());
            subDto.setSubmittedAt(sub.getSubmittedAt());
            subDto.setRuntime(sub.getRuntime());
            subDto.setMemory(sub.getMemory());
            subDto.setStatus(sub.getStatus());
            dto.setSubmission(subDto);
        }

        return dto;
    }
}
