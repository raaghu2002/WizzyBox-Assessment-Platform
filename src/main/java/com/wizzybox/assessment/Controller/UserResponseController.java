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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-responses")
@CrossOrigin(origins = "http://localhost:3000" , allowedHeaders = "*")
public class UserResponseController {
    private static final Logger logger = LoggerFactory.getLogger(UserResponseController.class);

    @Autowired
    private UserResponseRepository userResponseRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PostMapping("/submit")
    public ResponseEntity<Integer> submitResponses(@RequestBody List<UserResponse> responses) {
        try {
            // Set submission time for all responses
            Date submissionTime = new Date();
            for (UserResponse response : responses) {
                response.setSubmissionTime(submissionTime);

                // Explicitly load the full question entity to ensure we have the subject
                Question question = questionRepository.findById(Long.valueOf(response.getQuestion().getQuestionId()))
                        .orElseThrow(() -> new RuntimeException("Question not found"));

                // Set the subject from the question
                if (question.getSubject() != null) {
                    response.setSubject(question.getSubject());
                    logger.info("Setting subject to: " + question.getSubject() + " for question ID: " + question.getQuestionId());
                } else {
                    logger.warn("Question subject is null for question ID: " + question.getQuestionId());
                }
            }

            // Save all responses
            List<UserResponse> savedResponses = userResponseRepository.saveAll(responses);

            // Log for debugging
            for (UserResponse saved : savedResponses) {
                logger.info("Saved response ID: " + saved.getId() + ", Subject: " + saved.getSubject());
            }

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
            // Get the user's most recent responses directly with a single query
            List<UserResponse> userResponses = userResponseRepository.findMostRecentResponsesByUserId(userId);

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

                        // Create a HashMap with the needed data
                        Map<String, Object> data = new HashMap<>();
                        data.put("questionId", question.getQuestionId());
                        data.put("questionText", question.getQuestionText());
                        data.put("optionA", question.getOptionA());
                        data.put("optionB", question.getOptionB());
                        data.put("optionC", question.getOptionC());
                        data.put("optionD", question.getOptionD());
                        data.put("correctOption", String.valueOf(question.getCorrectOption()));
                        data.put("userResponse", response.getUserResponse());
                        data.put("subject", response.getSubject());  // Include the subject field

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

    @GetMapping("/has-attended/{userId}/{subject}")
    public ResponseEntity<Boolean> hasUserAttendedAssessment(@PathVariable String userId, @PathVariable String subject) {
        try {
            int count = userResponseRepository.countResponsesByUserIdAndSubject(userId, subject);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            logger.error("Error checking if user has attended assessment", e);
            return ResponseEntity.status(500).body(false);
        }
    }

}