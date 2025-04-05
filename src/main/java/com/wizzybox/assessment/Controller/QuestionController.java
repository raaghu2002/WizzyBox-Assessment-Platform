package com.wizzybox.assessment.Controller;

import com.wizzybox.assessment.Model.Question;
import com.wizzybox.assessment.Repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:3000")
public class QuestionController {

    @Autowired
    private QuestionRepository questionRepository;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadQuestions(@RequestParam("file") MultipartFile file) {
        try {
            List<Question> questions = new BufferedReader(new InputStreamReader(file.getInputStream()))
                    .lines()
                    .skip(1) // Skip header row
                    .map(line -> {
                        String[] data = line.split(",");
                        return new Question(
                                null,   // id is auto-generated
                                data[0], // subject
                                data[1], // questionText
                                data[2], // optionA
                                data[3], // optionB
                                data[4], // optionC
                                data[5], // optionD
                                data[6].charAt(0) // correctOption
                        );
                    })
                    .collect(Collectors.toList());

            questionRepository.saveAll(questions);
            return ResponseEntity.ok("Questions uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading questions");
        }
    }

    @GetMapping("/get")
    public List<Question> getQuestions(@RequestParam String subject) {
        List<Question> questions = questionRepository.findBySubject(subject);
        Collections.shuffle(questions);
        return questions;
    }

}

