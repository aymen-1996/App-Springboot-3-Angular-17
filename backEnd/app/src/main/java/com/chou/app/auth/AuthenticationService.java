package com.chou.app.auth;

import com.chou.app.email.EmailService;
import com.chou.app.security.JwtService;
import com.chou.app.user.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final String CONFIRMATION_URL = "http://localhost:8080/auth/confirm?token=%s";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;
    private final JavaMailSender mailSender;



    @Transactional
    public String register(RegistrationRequest registrationDto) {

        boolean userExists = userRepository.findByEmail(registrationDto.getEmail()).isPresent();
        if (userExists) {
            throw new IllegalStateException("A user already exists");
        }
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());

        Integer maxOrderNumber = userRepository.findMaxOrderNumber();
        int order = (maxOrderNumber != null) ? maxOrderNumber + 1 : 1;

        User applicationUser = User.builder()
                .firstname(registrationDto.getFirstname())
                .lastname(registrationDto.getLastname())
                .email(registrationDto.getEmail())
                .password(encodedPassword)
                .orderNumber(order)
                .createdDate(LocalDateTime.now())
                .role(UserRole.ROLE_USER)
                .build();

        User savedUser = userRepository.save(applicationUser);

        String generatedToken = UUID.randomUUID().toString();
        Token token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .user(savedUser)
                .build();
        tokenRepository.save(token);

        new Thread(() -> {
            try {
                emailService.send(
                        registrationDto.getEmail(),
                        registrationDto.getFirstname(),
                        null,
                        String.format(CONFIRMATION_URL, generatedToken)
                );
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }).start();

        return "User registered successfully";
    }



    public String confirm(String token) {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found"));

        if (savedToken.getUser().isEnabled()) {
            String message = "<div style='text-align: center; color: green; margin-top: 50vh; transform: translateY(-50%);'><h1>Your account is already activated</h1><a href='http://localhost:4200/login' style='text-decoration: none;'><button style='padding: 15px 25px; background-color: #007bff; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 18px;'>Return to Page Login</button></a></div>";

            return message;
        }

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt()) && !savedToken.getUser().isEnabled()) {
            String generatedToken = UUID.randomUUID().toString();
            Token newToken = Token.builder()
                    .token(generatedToken)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .user(savedToken.getUser())
                    .build();
            tokenRepository.save(newToken);

            try {
                emailService.send(
                        savedToken.getUser().getEmail(),
                        savedToken.getUser().getFirstname(),
                        null,
                        String.format(CONFIRMATION_URL, generatedToken)
                );
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            String message = "<div style='text-align: center; color: red; margin-top: 50vh; transform: translateY(-50%);'><h1>Token expired, a new token has been sent to your email</h1></div>";

            return message;
        }

        User user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        String message = "<div style='text-align: center; color: green; margin-top: 50vh; transform: translateY(-50%);'><h1>Your account has been successfully activated</h1><a href='http://localhost:4200/login' style='text-decoration: none;'><button style='padding: 15px 25px; background-color: #007bff; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 18px;'>Return to Page Login</button></a></div>";
        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
        return message;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            return AuthenticationResponse.builder()
                    .error("Email not found")
                    .build();
        }

        User user = userOptional.get();

        if (!user.isEnabled()) {
            return AuthenticationResponse.builder()
                    .error("Account not active. Check email for activation.")
                    .build();
        }

        boolean passwordCorrect = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!passwordCorrect) {
            return AuthenticationResponse.builder()
                    .error("Incorrect password")
                    .build();
        }

        var claims = new HashMap<String, Object>();
        claims.put("fullName", user.getFullName());

        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .build();
    }

    public CheckTokenResponse checkToken(String token) {
        CheckTokenResponse response = new CheckTokenResponse();
        response.setValid(false);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        response.setValid(!jwtService.isTokenExpired(token));
        return response;
    }

    public List<User> getAllUsersOrderedByOrderNumber() {
        return userRepository.findAllByOrderByOrderNumberAsc();
    }

    public void reorderUsers(List<User> updatedUsers) {
        userRepository.saveAll(updatedUsers);
    }

    public void sendPasswordResetCode(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String resetCode = generateRandomCode();
            user.setResetCode(resetCode);
            userRepository.save(user);

            Thread emailThread = new Thread(() -> {
                try {
                    SimpleMailMessage mailMessage = new SimpleMailMessage();
                    mailMessage.setTo(user.getEmail());
                    mailMessage.setSubject("Password Reset");
                    mailMessage.setText("Your password reset code is: " + resetCode);
                    mailSender.send(mailMessage);
                } catch (Exception e) {
                    throw new RuntimeException("Error sending password reset email.");
                }
            });

            emailThread.start();
        } else {
            throw new IllegalArgumentException("Email not found");
        }
    }


    @Transactional
    public boolean checkResetCode(String email, String resetCode) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getResetCode() != null && user.getResetCode().equals(resetCode)) {
                return true;
            }
        }
        return false;
    }


    public void resetPassword(String email, String resetCode, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("The new password and confirmation do not match.");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (resetCode.equals(user.getResetCode())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetCode(null);
                userRepository.save(user);
            } else {
                throw new IllegalArgumentException("Invalid reset code");
            }
        } else {
            throw new IllegalArgumentException("Email not found");
        }
    }



    @Transactional
    public void deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            tokenRepository.deleteByUser(user);
            userRepository.delete(user);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }
    private String generateRandomCode() {
        int randomCode = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomCode);
    }

}
