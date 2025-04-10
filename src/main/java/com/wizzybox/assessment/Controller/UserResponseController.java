package com.wizzybox.assessment.Controller;

import com.wizzybox.assessment.Model.Question;
import com.wizzybox.assessment.Model.UserResponse;
import com.wizzybox.assessment.Repository.QuestionRepository;
import com.wizzybox.assessment.Repository.UserResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    public ResponseEntity<?> submitResponses(@RequestBody List<UserResponse> responses) {
        try {
            if (responses == null || responses.isEmpty()) {
                return ResponseEntity.badRequest().body("No responses provided");
            }

            UserResponse firstResponse = responses.get(0);
            String userId = firstResponse.getUserId();
            Integer questionId = firstResponse.getQuestion().getQuestionId();

            // Load question to get subject
            Question firstQuestion = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));
            String subject = firstQuestion.getSubject();

            // Check if user already submitted for this subject
            boolean exists = userResponseRepository.existsByUserIdAndSubject(userId, subject);
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("User has already submitted responses for subject: " + subject);
            }

            Date submissionTime = new Date();

            for (UserResponse response : responses) {
                response.setSubmissionTime(submissionTime);

                Integer qid = response.getQuestion().getQuestionId();
                Question question = questionRepository.findById(qid)
                        .orElseThrow(() -> new RuntimeException("Question not found with ID: " + qid));

                response.setSubject(question.getSubject());
//                response.setExplanation(question.getExplanation());

                logger.info("Processed response for Question ID: {}, Subject: {}", qid, question.getSubject());
            }

            userResponseRepository.saveAll(responses);
            int score = calculateScore(responses);
            return ResponseEntity.ok(score);

        } catch (Exception e) {
            logger.error("Error submitting responses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong.");
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
                        Question question = questionRepository.findById(response.getQuestion().getQuestionId())
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
                        data.put("subject", response.getSubject());
                        data.put("explanation", question.getExplanation());

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
                Question question = questionRepository.findById(response.getQuestion().getQuestionId())
                        .orElse(null);

                if (question != null &&
                        response.getUserResponse().equals(String.valueOf(question.getCorrectOption()))) {
                    score++;
                }
            }
        }

        return score;
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/has-attended/{userId}/{subject}")
    public ResponseEntity<Boolean> existsByUserIdAndSubject(@PathVariable String userId, @PathVariable String subject) {
        try {
            int count = userResponseRepository.countResponsesByUserIdAndSubject(userId, subject);
            return ResponseEntity.ok(count > 0);
        } catch (Exception e) {
            // Consider logging the exception details for better debugging
            e.printStackTrace(); // Add this line to print the stack trace
            return ResponseEntity.status(500).body(false);
        }
    }

}