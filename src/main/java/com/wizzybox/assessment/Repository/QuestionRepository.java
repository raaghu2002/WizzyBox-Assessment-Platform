package com.wizzybox.assessment.Repository;

import com.wizzybox.assessment.Model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubject(String subject);


    Optional<Question> findById(Long questionId);
}

