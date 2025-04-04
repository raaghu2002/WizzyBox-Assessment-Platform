package com.wizzybox.assessment.Controller;

import com.wizzybox.assessment.Model.User;
import com.wizzybox.assessment.Service.UserService;
import com.wizzybox.assessment.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        return userService.registerUser(user.getName(), user.getEmail());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp, @RequestParam String password) {
        return userService.verifyOtpAndSetPassword(email, otp, password);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        return userService.sendResetOtp(email);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email,
                                                @RequestParam String otp,
                                                @RequestParam String newPassword) {
        return userService.verifyResetOtpAndSetPassword(email, otp, newPassword);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email, @RequestParam String password) {
        User user = userService.findByEmail(email);

        if (user != null && userService.verifyUser(email, password)) {
            String token = jwtUtil.generateToken(email);

            Map<String, Object> response = new HashMap<>();
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("token", token);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    }




    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            try {
                String username = jwtUtil.extractUsername(jwtToken);
                if (jwtUtil.validateToken(jwtToken)) {
                    return ResponseEntity.ok("Token is valid for user: " + username);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is invalid");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token validation error: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No token provided");
        }
    }


}

