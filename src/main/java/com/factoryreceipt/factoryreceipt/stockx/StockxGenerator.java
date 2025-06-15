package com.factoryreceipt.factoryreceipt.stockx;

import com.factoryreceipt.factoryreceipt.dto.StockxRequest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Klasa pomocnicza, w której "rozrzucamy" logikę generowania danych do maila StockX.
 */
public class StockxGenerator {

    // Ustawienie formatera walutowego z Locale.US, żeby separator dziesiętny był kropką
    private static final DecimalFormat currencyFormat;
    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(',');
        currencyFormat = new DecimalFormat("$#,##0.00", symbols);
    }

    public static Map<String, Object> generate(StockxRequest request) {
        // 1) Parsowanie ceny z uwzględnieniem różnych formatów (np. "$1355", "1,355", "1.355", "250,00")
        String priceInput = (request.getPrice() != null && !request.getPrice().isEmpty()) ? request.getPrice() : "0";
        double priceValue = parsePrice(priceInput);

        // 2) Losowanie fee, shipping, finalPrice, orderNumber
        Random random = new Random();
        double feePercentage = (4 + random.nextDouble() * (6 - 4)) / 100.0;
        double fee = Math.round(priceValue * feePercentage * 100.0) / 100.0;
        double shipping = 15.41 + random.nextDouble() * (24.94 - 15.41);
        shipping = Math.round(shipping * 100.0) / 100.0;
        double finalPrice = Math.round((priceValue + fee + shipping) * 100.0) / 100.0;

        long part1 = 10000000L + random.nextInt(90000000);
        long part2 = 10000000L + random.nextInt(90000000);
        String orderNumber = part1 + "-" + part2;

        // 3) Formatowanie cen przy użyciu DecimalFormat, co gwarantuje format np. "$1,355.00"
        String formattedPrice = currencyFormat.format(priceValue);
        String formattedFee = currencyFormat.format(fee);
        String formattedShipping = currencyFormat.format(shipping);
        String formattedFinalPrice = currencyFormat.format(finalPrice);

        // 4) Zdjęcie
        String photo = (request.getPhoto() != null && !request.getPhoto().isEmpty()) ? request.getPhoto() : "Brak";

        // 5) Budujemy mapę zmiennych
        Map<String, Object> variables = new HashMap<>();
        variables.put("productName",  request.getProductName());
        variables.put("price",        formattedPrice);
        variables.put("fee",          formattedFee);
        variables.put("shipping",     formattedShipping);
        variables.put("finalPrice",   formattedFinalPrice);
        variables.put("orderNumber",  orderNumber);
        variables.put("styleid",      request.getStyleid() != null ? request.getStyleid() : "Brak");
        variables.put("size",         request.getSize() != null ? request.getSize() : "Brak");
        variables.put("deliveryTime", request.getDeliveryTime() != null ? request.getDeliveryTime() : "Brak");
        variables.put("photo",        photo);

        return variables;
    }

    // Metoda pomocnicza, która analizuje wejściowy ciąg z ceną, uwzględniając różne formaty (przecinek jako separator tysięcy lub dziesiętny)
    private static double parsePrice(String input) {
        // Usuwamy znak dolara i ewentualne spacje
        String sanitized = input.replaceAll("[$\\s]", "");

        // Jeśli ciąg zawiera zarówno przecinek, jak i kropkę, ustalamy separator dziesiętny jako ten, który występuje później
        if (sanitized.contains(",") && sanitized.contains(".")) {
            int lastComma = sanitized.lastIndexOf(",");
            int lastDot = sanitized.lastIndexOf(".");
            if (lastComma > lastDot) {
                // Przecinek jest separatorem dziesiętnym, usuń kropki (jako separatory tysięcy) i zamień przecinek na kropkę
                sanitized = sanitized.replace(".", "");
                sanitized = sanitized.replace(",", ".");
            } else {
                // Kropka jest separatorem dziesiętnym, usuń przecinki
                sanitized = sanitized.replace(",", "");
            }
        } else if (sanitized.contains(",")) {
            // Jeśli występuje tylko przecinek – sprawdzamy, czy jest na pozycji separatora dziesiętnego
            int commaIndex = sanitized.lastIndexOf(",");
            if (commaIndex == sanitized.length() - 3) {
                // Przecinek jako separator dziesiętny – zamień go na kropkę
                sanitized = sanitized.replace(",", ".");
            } else {
                // Przecinek jako separator tysięcy – usuń go
                sanitized = sanitized.replace(",", "");
            }
        } else if (sanitized.contains(".")) {
            // Jeśli występuje tylko kropka – sprawdzamy, czy jest ona separatorem dziesiętnym
            int dotIndex = sanitized.lastIndexOf(".");
            if (dotIndex != sanitized.length() - 3) {
                // Kropka nie jest separatorem dziesiętnym – usuń ją
                sanitized = sanitized.replace(".", "");
            }
        }

        double value;
        try {
            value = Double.parseDouble(sanitized);
        } catch (NumberFormatException e) {
            value = 0.0;
        }
        return value;
    }
}
