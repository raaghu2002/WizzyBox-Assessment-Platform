package com.wizzybox.assessment.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "user_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private String userId;

    @Column(name = "user_response")
    private String userResponse;


    @Column(name = "subject")
    private String subject;

    @Column(name = "explanation")
    private String explanation;

    @Column(name = "submission_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submissionTime;

    // This is not needed as we can get it from the Question entity
    // @Column(name = "correct_option")
    // private Character correctOption;

    // This is not needed as we can get it from the Question entity
    // @Column(name = "question_text", columnDefinition = "TEXT")
    // private String questionText;
}