package com.wizzybox.assessment.Controller;

import com.wizzybox.assessment.Repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subjects")
@CrossOrigin(origins = "http://localhost:3000")
public class SubjectController {

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping
    public List<String> getAllSubjects() {
        // Get distinct subjects from the question repository
        return questionRepository.findDistinctSubjects();
    }
}