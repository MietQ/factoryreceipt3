package com.factoryreceipt.factoryreceipt.controller;

import com.factoryreceipt.factoryreceipt.model.User;
import com.factoryreceipt.factoryreceipt.model.Shop;
import com.factoryreceipt.factoryreceipt.repository.UserRepository;
import com.factoryreceipt.factoryreceipt.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    /**
     * Zwraca listę wszystkich użytkowników (wyświetlana w panelu admina).
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Aktualizuje dane użytkownika (email, limit, data wygaśnięcia).
     */
    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Aktualizacja e-maila
        if (request.getEmail() != null) {
            if (user.getLastEmailChange() != null) {
                LocalDateTime nextAllowedChange = user.getLastEmailChange().plusHours(24);
                if (LocalDateTime.now().isBefore(nextAllowedChange)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("E-mail można zmienić raz na 24 godziny. Następna zmiana możliwa od: " + nextAllowedChange);
                }
            }
            user.setEmail(request.getEmail());
            user.setLastEmailChange(LocalDateTime.now());
        }

        // Aktualizacja limitu potwierdzeń
        if (request.getUsageLimit() != null) {
            user.setUsageLimit(request.getUsageLimit());
        }

        // Aktualizacja daty wygaśnięcia
        if (request.getExpirationDate() != null) {
            user.setExpirationDate(request.getExpirationDate());
        }

        userRepository.save(user);
        return ResponseEntity.ok("Dane użytkownika zostały zaktualizowane");
    }

    // DTO do aktualizacji użytkownika
    public static class UserUpdateRequest {
        private String email;
        private Integer usageLimit;
        private LocalDateTime expirationDate;

        public String getEmail() {
            return email;
        }
        public void setEmail(String email) {
            this.email = email;
        }
        public Integer getUsageLimit() {
            return usageLimit;
        }
        public void setUsageLimit(Integer usageLimit) {
            this.usageLimit = usageLimit;
        }
        public LocalDateTime getExpirationDate() {
            return expirationDate;
        }
        public void setExpirationDate(LocalDateTime expirationDate) {
            this.expirationDate = expirationDate;
        }
    }

    // -------------------------------------------------------------------------
    // ZARZĄDZANIE SKLEPAMI
    // -------------------------------------------------------------------------

    /**
     * Klasa pomocnicza do przyjmowania danych o sklepie z frontendu.
     * Pola muszą odpowiadać danym z formularza w admin.html.
     */
    public static class ShopRequest {
        public String storeName;       // np. "MediaExpert"
        public String senderEmail;     // np. "no-reply@mediaexpert.pl"
        public String senderName;      // np. "MediaExpert"
        public String templateName;    // np. "mediaexpertTemplate"
        public String defaultSubject;  // np. "Twoje zamówienie"
        public String formConfig;      // JSON z konfiguracją formularza
    }

    /**
     * Zwraca listę wszystkich sklepów.
     */
    @GetMapping("/shops")
    public ResponseEntity<List<Shop>> getAllShops() {
        return ResponseEntity.ok(shopRepository.findAll());
    }

    /**
     * Dodaje nowy sklep do bazy danych.
     */
    @PostMapping("/shops")
    public ResponseEntity<?> createShop(@RequestBody ShopRequest request) {
        if (request.storeName == null || request.storeName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nazwa sklepu jest wymagana.");
        }

        Shop shop = new Shop();
        shop.setShopName(request.storeName);
        shop.setEmailFrom(request.senderEmail);
        shop.setSenderName(request.senderName);
        shop.setTemplateName(request.templateName);
        shop.setDefaultSubject(request.defaultSubject);
        shop.setFormConfig(request.formConfig);

        shopRepository.save(shop);
        return ResponseEntity.ok("Dodano nowy sklep: " + request.storeName);
    }
}
