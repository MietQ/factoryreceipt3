package com.factoryreceipt.factoryreceipt.controller;

import com.factoryreceipt.factoryreceipt.dto.AccountCreationRequest;
import com.factoryreceipt.factoryreceipt.dto.AccountCreationResponse;
import com.factoryreceipt.factoryreceipt.dto.AccountDto;
import com.factoryreceipt.factoryreceipt.model.User;
import com.factoryreceipt.factoryreceipt.model.Receipt;
import com.factoryreceipt.factoryreceipt.repository.ReceiptRepository;
import com.factoryreceipt.factoryreceipt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @GetMapping("/account/{userId}")
    public ResponseEntity<?> getAccount(@PathVariable String userId) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // Utwórz DTO z pełnymi danymi, w tym e-mailem
        AccountDto accountDto = new AccountDto(
                user.getUserId(),
                user.getEmail(),       // dodane pobieranie e-maila
                user.getAccountType(),
                user.getUsageLimit(),
                user.getExpirationDate()
        );
        return ResponseEntity.ok(accountDto);
    }


    @GetMapping("/receipts/{userId}")
    public ResponseEntity<?> getReceipts(@PathVariable String userId) {
        List<Receipt> receipts = receiptRepository.findByUserId(userId);
        return ResponseEntity.ok(receipts);
    }

    // NOWY endpoint do tworzenia konta
    @PostMapping("/createAccount")
    public ResponseEntity<AccountCreationResponse> createAccount(@RequestBody AccountCreationRequest request) {
        // Generujemy 5-cyfrowe userId i 5-znakowe hasło
        String generatedUserId = generateRandomDigits(5);
        String generatedPassword = generateRandomAlphaNumeric(5);

        String accountType = request.getAccountType();
        LocalDateTime expirationDate = null;
        Integer usageLimit = null;

        if ("time".equalsIgnoreCase(accountType)) {
            // Dla kont czasowych: domyślnie 1 dzień, jeśli nie podano
            int days = (request.getDurationDays() != null && request.getDurationDays() > 0) ? request.getDurationDays() : 1;
            expirationDate = LocalDateTime.now().plusDays(days);
        } else if ("limit".equalsIgnoreCase(accountType)) {
            // Dla kont limitowanych: liczba użyć, domyślnie 1
            usageLimit = (request.getUsageLimit() != null && request.getUsageLimit() > 0) ? request.getUsageLimit() : 1;
        } else if ("lifetime".equalsIgnoreCase(accountType)) {
            // Konto lifetime - brak wygaśnięcia i limitu
            // Możesz ustawić te pola jako null lub jako specjalne wartości
        } else {
            return ResponseEntity.badRequest().build();
        }

        // Tworzymy nowe konto (User)
        User user = new User();
        user.setUserId(generatedUserId);
        user.setPassword(generatedPassword);
        user.setAccountType(accountType);
        user.setExpirationDate(expirationDate);
        user.setUsageLimit(usageLimit);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        AccountCreationResponse response = new AccountCreationResponse(
                generatedUserId,
                generatedPassword,
                accountType,
                expirationDate,
                usageLimit
        );

        return ResponseEntity.ok(response);
    }

    // Metoda pomocnicza: generowanie 5 losowych cyfr
    private String generateRandomDigits(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // Metoda pomocnicza: generowanie 5 losowych znaków (litery i cyfry)
    private String generateRandomAlphaNumeric(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
