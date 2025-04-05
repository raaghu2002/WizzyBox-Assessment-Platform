package com.wizzybox.assessment.Repository;

import com.wizzybox.assessment.Model.UserResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserResponseRepository extends JpaRepository<UserResponse, Long> {
    List<UserResponse> findByUserId(String userId);
}
