package com.vijay.User_Master.service;

import com.vijay.User_Master.dto.tutorial.UserProgressDTO;
import com.vijay.User_Master.entity.Tutorial;
import com.vijay.User_Master.entity.User;
import com.vijay.User_Master.entity.UserProgress;
import com.vijay.User_Master.repository.TutorialRepository;
import com.vijay.User_Master.repository.UserProgressRepository;
import com.vijay.User_Master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProgressService {

    private final UserProgressRepository progressRepository;
    private final TutorialRepository tutorialRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public List<UserProgressDTO> getUserProgress(Long userId) {
        return progressRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProgressDTO> getCurrentUserProgress() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        
        return getUserProgress(user.getId());
    }

    @Transactional(readOnly = true)
    public UserProgressDTO getTutorialProgress(Long userId, Long tutorialId) {
        UserProgress progress = progressRepository.findByUserIdAndTutorialId(userId, tutorialId)
                .orElse(null);
        
        return progress != null ? convertToDTO(progress) : null;
    }

    @Transactional
    public UserProgressDTO startTutorial(Long tutorialId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new RuntimeException("Tutorial not found"));

        // Check if progress already exists
        UserProgress progress = progressRepository.findByUserIdAndTutorialId(user.getId(), tutorialId)
                .orElse(null);

        if (progress == null) {
            progress = UserProgress.builder()
                    .user(user)
                    .tutorial(tutorial)
                    .progressPercentage(0)
                    .startedAt(LocalDateTime.now())
                    .build();
            
            progress = progressRepository.save(progress);
            log.info("Started tutorial {} for user {}", tutorialId, username);
        }

        return convertToDTO(progress);
    }

    @Transactional
    public UserProgressDTO updateProgress(Long tutorialId, Integer percentage) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        UserProgress progress = progressRepository.findByUserIdAndTutorialId(user.getId(), tutorialId)
                .orElseThrow(() -> new RuntimeException("Progress not found. Start the tutorial first."));

        progress.setProgressPercentage(percentage);
        progress.setLastAccessedAt(LocalDateTime.now());

        if (percentage >= 100 && !progress.isCompleted()) {
            progress.markAsCompleted();
            log.info("User {} completed tutorial {}", username, tutorialId);
        }

        progress = progressRepository.save(progress);
        return convertToDTO(progress);
    }

    @Transactional
    public UserProgressDTO completeTutorial(Long tutorialId) {
        return updateProgress(tutorialId, 100);
    }

    @Transactional(readOnly = true)
    public Long getCompletedTutorialsCount(Long userId) {
        return progressRepository.countCompletedTutorialsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Long getTotalTimeSpent(Long userId) {
        Long totalMinutes = progressRepository.getTotalTimeSpentByUserId(userId);
        return totalMinutes != null ? totalMinutes : 0L;
    }

    @Transactional(readOnly = true)
    public Double getAverageProgress(Long userId) {
        Double average = progressRepository.getAverageProgressByUserId(userId);
        return average != null ? average : 0.0;
    }

    private UserProgressDTO convertToDTO(UserProgress progress) {
        UserProgressDTO dto = modelMapper.map(progress, UserProgressDTO.class);
        
        if (progress.getUser() != null) {
            dto.setUserId(progress.getUser().getId());
            dto.setUserName(progress.getUser().getName());
        }
        
        if (progress.getTutorial() != null) {
            dto.setTutorialId(progress.getTutorial().getId());
            dto.setTutorialTitle(progress.getTutorial().getTitle());
        }
        
        return dto;
    }
}
