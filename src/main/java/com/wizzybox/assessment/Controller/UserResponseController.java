package com.wizzybox.assessment.Controller;

import com.wizzybox.assessment.Model.Question;
import com.wizzybox.assessment.Model.UserResponse;
import com.wizzybox.assessment.Repository.QuestionRepository;
import com.wizzybox.assessment.Repository.UserResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-responses")
@CrossOrigin(origins = "http://localhost:3000")
public class UserResponseController {
    private static final Logger logger = LoggerFactory.getLogger(UserResponseController.class);

    @Autowired
    private UserResponseRepository userResponseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PostMapping("/submit")
    public ResponseEntity<Integer> submitResponses(@RequestBody List<UserResponse> responses) {
        try {
            // Save all responses
            userResponseRepository.saveAll(responses);

            // Calculate score
            int score = calculateScore(responses);

            return ResponseEntity.ok(score);
        } catch (Exception e) {
            logger.error("Error submitting responses", e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/review/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getReviewData(@PathVariable String userId) {
        try {
            // Get the user's most recent responses
            List<UserResponse> userResponses = userResponseRepository.findByUserId(userId);

            if (userResponses.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Create a list to collect questions with user responses and correct answers
            List<Map<String, Object>> reviewData = userResponses.stream()
                    .map(response -> {
                        // Get the question details
                        Question question = questionRepository.findById(Long.valueOf(response.getQuestion().getQuestionId()))
                                .orElse(null);

                        if (question == null) {
                            return null;
                        }

                        // Create a HashMap instead of using Map.of()
                        Map<String, Object> data = new HashMap<>();
                        data.put("questionId", question.getQuestionId());
                        data.put("questionText", question.getQuestionText());
                        data.put("optionA", question.getOptionA());
                        data.put("optionB", question.getOptionB());
                        data.put("optionC", question.getOptionC());
                        data.put("optionD", question.getOptionD());
                        data.put("correctOption", String.valueOf(question.getCorrectOption()));
                        data.put("userResponse", response.getUserResponse());

                        return data;
                    })
                    .filter(data -> data != null)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(reviewData);
        } catch (Exception e) {
            logger.error("Error retrieving review data for user: {}", userId, e);
            return ResponseEntity.status(500).body(null);
        }
    }

    private int calculateScore(List<UserResponse> responses) {
        int score = 0;

        for (UserResponse response : responses) {
            if (response.getUserResponse() != null) {
                // Fetch the question to get the correct answer
                // Convert Integer to Long for repository call
                Question question = questionRepository.findById(Long.valueOf(response.getQuestion().getQuestionId()))
                        .orElse(null);

                if (question != null &&
                        response.getUserResponse().equals(String.valueOf(question.getCorrectOption()))) {
                    score++;
                }
            }
        }

        return score;
    }
}