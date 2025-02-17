package com.factoryreceipt.factoryreceipt.controller;

import com.factoryreceipt.factoryreceipt.dto.StockxRequest;
import com.factoryreceipt.factoryreceipt.model.Receipt;
import com.factoryreceipt.factoryreceipt.model.User;
import com.factoryreceipt.factoryreceipt.repository.ReceiptRepository;
import com.factoryreceipt.factoryreceipt.repository.UserRepository;
import com.factoryreceipt.factoryreceipt.service.EmailService;
import com.factoryreceipt.factoryreceipt.stockx.StockxGenerator;
import com.factoryreceipt.factoryreceipt.customshops.MediaExpertGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    /**
     * Uniwersalny endpoint generowania potwierdzenia dla sklepu określonego w ścieżce.
     * W zależności od wartości shopName wywoływana jest odpowiednia klasa generatora.
     */
    @PostMapping("/{shopName}")
    public ResponseEntity<String> generateConfirmation(@PathVariable String shopName,
                                                       @RequestBody Map<String, String> request) {
        // 1) Walidacja userId
        String userId = request.get("userId");
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Brak identyfikatora użytkownika.");
        }
        // 2) Pobieramy użytkownika
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Użytkownik nie istnieje.");
        }
        // 3) Sprawdzamy limit dla kont typu "limit"
        if ("limit".equalsIgnoreCase(user.getAccountType())) {
            Integer currentLimit = user.getUsageLimit();
            if (currentLimit == null || currentLimit <= 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Limit potwierdzeń został wyczerpany.");
            }
        }
        // 4) Generujemy zmienne – wybieramy generator w zależności od shopName
        Map<String, Object> variables;
        try {
            if ("StockX".equalsIgnoreCase(shopName)) {
                variables = StockxGenerator.generate((StockxRequest) request);
            } else if ("MediaExpert".equalsIgnoreCase(shopName)) {
                variables = MediaExpertGenerator.generate(request);
            } else {
                return ResponseEntity.badRequest().body("Sklep nieobsługiwany: " + shopName);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Błąd podczas generowania danych: " + e.getMessage());
        }
        // 5) Wysyłamy e-mail – używamy e-maila z bazy użytkownika
        String actualEmail = user.getEmail();
        if (actualEmail != null && !actualEmail.isEmpty()) {
            emailService.sendStockxEmail(actualEmail, null, variables, shopName);
        }
        // 6) Aktualizacja limitu (dla kont typu "limit")
        if ("limit".equalsIgnoreCase(user.getAccountType())) {
            Integer currentLimit = user.getUsageLimit();
            if (currentLimit != null && currentLimit > 0) {
                user.setUsageLimit(currentLimit - 1);
                userRepository.save(user);
            }
        }
        // 7) Zapis historii potwierdzenia
        Receipt receipt = new Receipt(
                userId,
                request.get("productName"),
                (String) variables.get("price"),
                (String) variables.get("fee"),
                (String) variables.get("shipping"),
                (String) variables.get("finalPrice"),
                (String) variables.get("orderNumber"),
                request.get("styleid"),
                request.get("size"),
                request.get("deliveryTime"),
                request.get("photo") != null ? request.get("photo") : "Brak",
                LocalDateTime.now()
        );
        receiptRepository.save(receipt);
        return ResponseEntity.ok("Potwierdzenie wysłane na Twój adres e-mail.");
    }
}
