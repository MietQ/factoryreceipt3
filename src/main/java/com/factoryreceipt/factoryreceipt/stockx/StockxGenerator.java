package com.factoryreceipt.factoryreceipt.stockx;

import com.factoryreceipt.factoryreceipt.dto.StockxRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Klasa pomocnicza, w której "rozrzucamy" logikę generowania danych do maila StockX.
 */
public class StockxGenerator {

    public static Map<String, Object> generate(StockxRequest request) {
        // 1) Parsowanie ceny
        String priceInput = (request.price != null && !request.price.isEmpty()) ? request.price : "0";
        String cleanPrice = priceInput.replaceAll("[^\\d.]", "");
        double priceValue;
        try {
            priceValue = cleanPrice.isEmpty() ? 0.0 : Double.parseDouble(cleanPrice);
        } catch (NumberFormatException e) {
            priceValue = 0.0;
        }

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

        // 3) Formatowanie cen
        String formattedPrice = (priceValue == Math.floor(priceValue))
                ? "$" + (int) priceValue
                : String.format("$%.2f", priceValue);
        String formattedFee = (fee == Math.floor(fee))
                ? "$" + (int) fee
                : String.format("$%.2f", fee);
        String formattedShipping = (shipping == Math.floor(shipping))
                ? "$" + (int) shipping
                : String.format("$%.2f", shipping);
        String formattedFinalPrice = (finalPrice == Math.floor(finalPrice))
                ? "$" + (int) finalPrice
                : String.format("$%.2f", finalPrice);

        // 4) Zdjęcie
        String photo = (request.photo != null && !request.photo.isEmpty()) ? request.photo : "Brak";

        // 5) Budujemy mapę zmiennych
        Map<String, Object> variables = new HashMap<>();
        variables.put("productName",  request.productName);
        variables.put("price",        formattedPrice);
        variables.put("fee",          formattedFee);
        variables.put("shipping",     formattedShipping);
        variables.put("finalPrice",   formattedFinalPrice);
        variables.put("orderNumber",  orderNumber);
        variables.put("styleid",      request.styleid != null ? request.styleid : "Brak");
        variables.put("size",         request.size != null ? request.size : "Brak");
        variables.put("deliveryTime", request.deliveryTime != null ? request.deliveryTime : "Brak");
        variables.put("photo",        photo);

        return variables;
    }
}
