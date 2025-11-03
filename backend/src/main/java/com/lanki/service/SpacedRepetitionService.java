package com.lanki.service;

import com.lanki.dto.DailyProblemDTO;
import com.lanki.dto.ProblemDTO;
import com.lanki.model.Interview;
import com.lanki.model.Problem;
import com.lanki.model.SpacedRepetitionCard;
import com.lanki.model.User;
import com.lanki.repository.SpacedRepetitionCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements SuperMemo 2 (SM-2) algorithm for spaced repetition.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    private final SpacedRepetitionCardRepository cardRepository;

    /**
     * Gets all problems due for review today or overdue.
     */
    @Transactional(readOnly = true)
    public List<DailyProblemDTO> getDailyRecommendations(User user) {
        LocalDate today = LocalDate.now();
        List<SpacedRepetitionCard> dueCards = cardRepository.findDueCardsByUserId(user.getId(), today);

        return dueCards.stream()
                .map(card -> {
                    DailyProblemDTO dto = new DailyProblemDTO();
                    dto.setProblem(ProblemDTO.fromEntity(card.getProblem()));
                    dto.setNextReviewDate(card.getNextReviewDate());
                    dto.setRepetitions(card.getRepetitions());

                    if (card.getLastReviewDate() != null) {
                        long daysSince = ChronoUnit.DAYS.between(
                            card.getLastReviewDate().toLocalDate(),
                            LocalDate.now()
                        );
                        dto.setDaysSinceLastReview((int) daysSince);
                    }

                    dto.setIsOverdue(today.isAfter(card.getNextReviewDate()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Creates or gets spaced repetition card for a problem.
     */
    @Transactional
    public SpacedRepetitionCard getOrCreateCard(User user, Problem problem) {
        return cardRepository.findByUserAndProblem(user, problem)
                .orElseGet(() -> {
                    SpacedRepetitionCard card = new SpacedRepetitionCard();
                    card.setUser(user);
                    card.setProblem(problem);
                    card.setNextReviewDate(LocalDate.now());
                    return cardRepository.save(card);
                });
    }

    /**
     * Updates spaced repetition card based on interview performance.
     * Called after interview is completed.
     */
    @Transactional
    public void updateCardAfterInterview(User user, Problem problem, Interview interview) {
        SpacedRepetitionCard card = getOrCreateCard(user, problem);

        // Convert interview score to SM-2 quality (0-5)
        int quality = interview.toSM2Quality();

        // Update card using SM-2 algorithm
        card.review(quality);
        cardRepository.save(card);

        log.info("Updated SM-2 card for user {} problem {} - quality: {}, next review: {}",
            user.getId(), problem.getId(), quality, card.getNextReviewDate());
    }

    /**
     * Manually review a card (for testing or manual adjustments).
     */
    @Transactional
    public void reviewCard(User user, Problem problem, int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }

        SpacedRepetitionCard card = getOrCreateCard(user, problem);
        card.review(quality);
        cardRepository.save(card);
    }

    /**
     * Gets count of problems due today.
     */
    @Transactional(readOnly = true)
    public long getCountOfDueProblems(User user) {
        return cardRepository.countDueCardsByUserId(user.getId(), LocalDate.now());
    }

    /**
     * Gets all cards for a user ordered by next review date.
     */
    @Transactional(readOnly = true)
    public List<SpacedRepetitionCard> getAllCards(User user) {
        return cardRepository.findAllCardsByUserIdOrderedByReviewDate(user.getId());
    }
}
