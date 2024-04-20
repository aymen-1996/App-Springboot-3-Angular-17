package com.chou.app.auth;

import com.chou.app.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
private final AuthenticationService service;

    @GetMapping("/all")
    public List<User> getAllUsersOrderedByOrderNumber() {
        return service.getAllUsersOrderedByOrderNumber();
    }

@DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        service.deleteUser(userId);
    }
    @PutMapping("/reorder")
    public ResponseEntity<Map<String, String>> reorderUsers(@RequestBody List<User> users) {
        try {
            service.reorderUsers(users);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ordre des utilisateurs mis à jour avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors de la mise à jour de l'ordre des utilisateurs : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
