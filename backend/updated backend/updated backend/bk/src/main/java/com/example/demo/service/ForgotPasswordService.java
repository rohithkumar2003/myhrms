package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // store email → otp + expiry
    private final Map<String, OtpData> otpStorage = new HashMap<>();

    // helper class for OTP storage
    private static class OtpData {
        String otp;
        long expiryTime; // millis
    }

    // Step 1: Generate & send OTP
    public String sendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate 6-digit OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        // Save OTP with 5 min expiry
        OtpData data = new OtpData();
        data.otp = otp;
        data.expiryTime = System.currentTimeMillis() + (2 * 60 * 1000);
        otpStorage.put(email, data);

        // Send email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + ". It will expire in 2 minutes.");
        mailSender.send(message);

        return otp; // return for testing only (remove in prod)
    }

    // Step 2: Verify OTP
    public boolean verifyOtp(String email, String otp) {
        OtpData data = otpStorage.get(email);
        if (data == null) return false;

        boolean valid = data.otp.equals(otp) && data.expiryTime > System.currentTimeMillis();
        return valid;
    }

    // Step 3: Reset Password
    public void resetPassword(String email, String otp, String newPassword) {
        // verify otp first
        if (!verifyOtp(email, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // clear OTP after successful reset
        otpStorage.remove(email);
    }
}
