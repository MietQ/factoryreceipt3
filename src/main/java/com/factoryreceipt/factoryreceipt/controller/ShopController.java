package com.factoryreceipt.factoryreceipt.controller;

import com.factoryreceipt.factoryreceipt.dto.StockxRequest;
import com.factoryreceipt.factoryreceipt.model.Receipt;
import com.factoryreceipt.factoryreceipt.model.User;
import com.factoryreceipt.factoryreceipt.repository.ReceiptRepository;
import com.factoryreceipt.factoryreceipt.repository.UserRepository;
import com.factoryreceipt.factoryreceipt.service.EmailService;
import com.factoryreceipt.factoryreceipt.stockx.StockxGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @PostMapping("/stockx")
    public ResponseEntity<String> generateStockxConfirmation(@RequestBody StockxRequest request) {

        // 1) Walidacja userId
        if (request.userId == null || request.userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Nie podano identyfikatora konta (userId).");
        }

        // 2) Pobieramy użytkownika
        User user = userRepository.findByUserId(request.userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Konto o podanym userId nie istnieje.");
        }

        // 3) Sprawdzamy limit (tylko dla kont typu "limit")
        if ("limit".equalsIgnoreCase(user.getAccountType())) {
            Integer currentLimit = user.getUsageLimit();
            if (currentLimit == null || currentLimit <= 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Limit potwierdzeń został wyczerpany. Nie możesz wygenerować kolejnego potwierdzenia.");
            }
        }

        // 4) Generujemy zmienne przy pomocy klasy StockxGenerator
        Map<String, Object> variables = StockxGenerator.generate(request);

        // 5) Wysyłka maila – używamy e-maila z bazy użytkownika
        String actualEmail = user.getEmail();
        if (actualEmail != null && !actualEmail.isEmpty()) {
            emailService.sendStockxEmail(actualEmail, null, variables, "StockX");
        }

        // 6) Aktualizacja limitu (dla kont 'limit')
        if ("limit".equalsIgnoreCase(user.getAccountType())) {
            Integer currentLimit = user.getUsageLimit();
            if (currentLimit != null && currentLimit > 0) {
                user.setUsageLimit(currentLimit - 1);
                userRepository.save(user);
            }
        }

        // 7) Zapis historii potwierdzenia
        Receipt receipt = new Receipt(
                request.userId,
                request.productName,
                (String) variables.get("price"),
                (String) variables.get("fee"),
                (String) variables.get("shipping"),
                (String) variables.get("finalPrice"),
                (String) variables.get("orderNumber"),
                request.styleid,
                request.size,
                request.deliveryTime,
                request.photo != null ? request.photo : "Brak",
                LocalDateTime.now()
        );
        receiptRepository.save(receipt);

        return ResponseEntity.ok("Potwierdzenie wysłane na Twój adres e-mail.");
    }
}
