package com.wizzybox.assessment.Service;

import com.wizzybox.assessment.Model.User;
import com.wizzybox.assessment.Repository.UserRepository;
import com.wizzybox.assessment.Util.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, JavaMailSender mailSender, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<String> registerUser(String name, String email) {
        if(userRepository.existsById(email)) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setName(name);

        String otp = generateOTP();
        user.setVerificationToken(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        try {
            sendOtpEmail(email, otp);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to send OTP email");
        }

        userRepository.save(user);
        return ResponseEntity.ok("OTP sent to registered email");
    }

    public ResponseEntity<String> verifyOtpAndSetPassword(String email, String otp, String password) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getVerificationToken().equals(otp) || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

        if (!isValidPassword(password)) {
            return ResponseEntity.badRequest().body("Password must be 8-12 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setVerified(true);
        user.setOtpExpiry(null);
        user.setVerificationToken(null);

        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(email);
        return ResponseEntity.ok("Registration successful. Token: " + token);
    }

    public ResponseEntity<String> verifyResetOtpAndSetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getVerificationToken().equals(otp) || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }

        if (!isValidPassword(newPassword)) {
            return ResponseEntity.badRequest().body("Password must be 8-12 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtpExpiry(null);
        user.setVerificationToken(null);
        userRepository.save(user);
        return ResponseEntity.ok("Password reset successful");
    }

    private boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,12}$";
        return password.matches(passwordRegex);
    }

    private String generateOTP() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    private void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(to);
            helper.setSubject("Your Verification OTP");
            helper.setText("Your OTP is: " + otp + "\nValid for 5 minutes");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    public boolean verifyUser(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        return optionalUser.map(user -> passwordEncoder.matches(password, user.getPassword())).orElse(false);
    }

    public ResponseEntity<String> sendResetOtp(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Email not registered");
        }

        String otp = generateOTP();
        user.setVerificationToken(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        try {
            sendOtpEmail(email, otp); // Send OTP via email
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to send OTP email");
        }

        userRepository.save(user);

        return ResponseEntity.ok("OTP sent to registered email for password reset");
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}