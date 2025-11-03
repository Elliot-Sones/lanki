package com.lanki.service;

import com.lanki.dto.ProblemDTO;
import com.lanki.model.Problem;
import com.lanki.model.User;
import com.lanki.model.UserProgress;
import com.lanki.repository.ProblemRepository;
import com.lanki.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final UserProgressRepository userProgressRepository;

    @Transactional(readOnly = true)
    public List<ProblemDTO> getAllProblems() {
        return problemRepository.findAll()
                .stream()
                .map(ProblemDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, List<ProblemDTO>> getProblemsGroupedByCategory(User user) {
        List<Problem> problems = problemRepository.findAll();
        Map<Long, UserProgress> progressMap = userProgressRepository.findByUser(user)
                .stream()
                .collect(Collectors.toMap(
                    up -> up.getProblem().getId(),
                    up -> up
                ));

        return problems.stream()
                .map(problem -> {
                    ProblemDTO dto = ProblemDTO.fromEntity(problem);
                    UserProgress progress = progressMap.get(problem.getId());
                    if (progress != null) {
                        dto.setProgressStatus(progress.getStatus().name());
                    } else {
                        dto.setProgressStatus("NOT_STARTED");
                    }
                    return dto;
                })
                .collect(Collectors.groupingBy(ProblemDTO::getCategory));
    }

    @Transactional(readOnly = true)
    public Problem getProblemById(Long id) {
        return problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Problem getProblemBySlug(String slug) {
        return problemRepository.findByTitleSlug(slug)
                .orElseThrow(() -> new RuntimeException("Problem not found with slug: " + slug));
    }

    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return problemRepository.findAllCategories();
    }
}
