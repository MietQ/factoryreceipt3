package com.factoryreceipt.factoryreceipt.customshops;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MediaExpertGenerator {

    public static Map<String, Object> generate(Map<String, String> request) {
        Map<String, Object> variables = new HashMap<>();
        // Generowanie numeru zamówienia – przykładowo 11-cyfrowy numer
        long randomNum = 10000000000L + (long)(new Random().nextDouble() * 90000000000L);
        variables.put("orderNumber", Math.abs(randomNum));
        // Przekazanie pól z request
        variables.put("email", request.get("email"));
        variables.put("productName", request.get("productName"));
        variables.put("price", request.get("price"));
        variables.put("productCode", request.get("productCode"));
        variables.put("photo", request.get("photo") != null && !request.get("photo").isEmpty() ? request.get("photo") : "Brak");
        variables.put("deliveryTime", request.get("deliveryTime"));
        variables.put("address", request.get("address"));
        variables.put("city", request.get("city"));
        variables.put("phoneNumber", request.get("phoneNumber"));
        return variables;
    }
}
