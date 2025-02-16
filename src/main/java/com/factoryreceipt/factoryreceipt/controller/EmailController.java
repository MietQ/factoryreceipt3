package com.factoryreceipt.factoryreceipt.controller;

import com.factoryreceipt.factoryreceipt.model.User;
import com.factoryreceipt.factoryreceipt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/account")
public class EmailController {

    @Autowired
    private UserRepository userRepository;

    // Klasa do przyjmowania danych JSON: { "userId": "...", "newEmail": "..." }
    public static class UpdateEmailRequest {
        public String userId;
        public String newEmail;
    }

    @PutMapping("/updateEmail")
    public ResponseEntity<?> updateEmail(@RequestBody UpdateEmailRequest request) {
        // 1. Znajdź użytkownika
        User user = userRepository.findByUserId(request.userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // 2. Sprawdź 24-godzinny odstęp
        LocalDateTime now = LocalDateTime.now();
        if (user.getLastEmailChange() != null) {
            LocalDateTime earliestNextChange = user.getLastEmailChange().plusHours(1);
            if (now.isBefore(earliestNextChange)) {
                // oblicz, ile godzin zostało
                long hoursLeft = Duration.between(now, earliestNextChange).toHours();
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only change your email once every 1 hour. Try again in ~" + hoursLeft + " hours.");
            }
        }

        // 3. Ustaw nowy email i lastEmailChange
        user.setEmail(request.newEmail);
        user.setLastEmailChange(now);

        // 4. Zapisz w bazie
        userRepository.save(user);

        return ResponseEntity.ok("Email updated successfully");
    }
}
