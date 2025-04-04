package com.wizzybox.assessment.Controller;

import com.wizzybox.assessment.Model.User;
import com.wizzybox.assessment.Service.UserService;
import com.wizzybox.assessment.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // Constructor injection for dependencies
    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // Registration endpoint
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        return userService.registerUser(user.getName(), user.getEmail());
    }

    // OTP verification endpoint
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp, @RequestParam String password) {
        return userService.verifyOtpAndSetPassword(email, otp, password);
    }

    // Forgot password - Send OTP
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return userService.sendResetOtp(email);
    }

    // Reset password - Verify OTP and set new password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email,
                                                @RequestParam String otp,
                                                @RequestParam String newPassword) {
        return userService.verifyResetOtpAndSetPassword(email, otp, newPassword);
    }


    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        User user = userService.findByEmail(email); // Fetch user details from the database

        if (user != null && userService.verifyUser(email, password)) {
            String token = jwtUtil.generateToken(email);

            // Create response JSON
            Map<String, Object> response = new HashMap<>();
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("token", token);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }

}
