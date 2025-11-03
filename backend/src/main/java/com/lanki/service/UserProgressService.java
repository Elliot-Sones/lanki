package com.lanki.service;

import com.lanki.model.Problem;
import com.lanki.model.User;
import com.lanki.model.UserProgress;
import com.lanki.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserProgressService {

    private final UserProgressRepository progressRepository;

    @Transactional
    public void markAsInProgress(User user, Problem problem) {
        UserProgress progress = progressRepository.findByUserAndProblem(user, problem)
                .orElse(new UserProgress());

        if (progress.getId() == null) {
            progress.setUser(user);
            progress.setProblem(problem);
        }

        progress.setStatus(UserProgress.ProgressStatus.IN_PROGRESS);
        progress.setAttemptCount(progress.getAttemptCount() + 1);
        progress.setLastAttemptedAt(LocalDateTime.now());

        progressRepository.save(progress);
    }

    @Transactional
    public void markAsCompleted(User user, Problem problem) {
        UserProgress progress = progressRepository.findByUserAndProblem(user, problem)
                .orElse(new UserProgress());

        if (progress.getId() == null) {
            progress.setUser(user);
            progress.setProblem(problem);
            progress.setAttemptCount(1);
        }

        progress.setStatus(UserProgress.ProgressStatus.COMPLETED);
        progress.setLastAttemptedAt(LocalDateTime.now());
        progress.setCompletedAt(LocalDateTime.now());

        progressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public UserProgress getProgress(User user, Problem problem) {
        return progressRepository.findByUserAndProblem(user, problem)
                .orElse(null);
    }
}
