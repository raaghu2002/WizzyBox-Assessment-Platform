package com.wizzybox.assessment.Repository;

import com.wizzybox.assessment.Model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findBySubject(String subject);

    Optional<Question> findById(Integer questionId);

    @Query("SELECT DISTINCT q.subject FROM Question q ORDER BY q.subject")
    List<String> findDistinctSubjects();
}

