package com.chou.app.auth;

import com.chou.app.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {

    private final AuthenticationService service;


    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegistrationRequest registrationDto) {
        try {
            String registrationResponse = service.register(registrationDto);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", registrationResponse);
            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }



    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(
            @RequestParam String token
    ) {
        return ResponseEntity.ok(service.confirm(token));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @GetMapping("/check-token/{token}")
    public CheckTokenResponse checkToken(@PathVariable String token) {
        return service.checkToken(token);
    }


    @PostMapping("/request-code")
    public ResponseEntity<String> requestCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        try {
            service.sendPasswordResetCode(email);
            return ResponseEntity.ok("A password reset code has been sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body( e.getMessage());
        }

    }
    @PostMapping("/check-reset-code")
    public ResponseEntity<String> checkResetCode(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String resetCode = requestBody.get("resetCode");

        boolean isValid = service.checkResetCode(email, resetCode);
        if (isValid) {
            return ResponseEntity.ok("Reset code is valid");
        } else {
            return ResponseEntity.badRequest().body("Invalid reset code");
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String resetCode = requestBody.get("resetCode");
        String newPassword = requestBody.get("newPassword");
        String confirmPassword = requestBody.get("confirmPassword");

        try {
            service.resetPassword(email, resetCode, newPassword, confirmPassword);
            return ResponseEntity.ok("Password reset successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(  e.getMessage());
        }
    }
}
