package com.wizzybox.assessment.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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

    @Column(nullable = false)
    private String username;

    @Column(name = "user_response")
    private String userResponse;

    @Column(name = "correct_option")
    private Character correctOption;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;
}
