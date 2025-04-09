package com.wizzybox.assessment.Repository;

import com.wizzybox.assessment.Model.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {

    // Get all responses for a user (which you don't want)
    List<UserResponse> findByUserId(String userId);

    // Get the latest submission time for a user
    @Query("SELECT MAX(ur.submissionTime) FROM UserResponse ur WHERE ur.userId = :userId")
    Date findLatestSubmissionTimeByUserId(@Param("userId") String userId);

    // Get responses for a user with a specific submission time
    List<UserResponse> findByUserIdAndSubmissionTime(String userId, Date submissionTime);

    // Alternative: Get most recent responses directly
    @Query("SELECT ur FROM UserResponse ur WHERE ur.userId = :userId AND ur.submissionTime = " +
            "(SELECT MAX(ur2.submissionTime) FROM UserResponse ur2 WHERE ur2.userId = :userId)")
    List<UserResponse> findMostRecentResponsesByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(ur) FROM UserResponse ur WHERE ur.userId = :userId AND ur.subject = :subject")
    int countResponsesByUserIdAndSubject(@Param("userId") String userId, @Param("subject") String subject);


    @Query("SELECT COUNT(u) > 0 FROM UserResponse u WHERE u.userId = :userId AND u.subject = :subject")
    boolean existsByUserIdAndSubject(@Param("userId") String userId, @Param("subject") String subject);




}