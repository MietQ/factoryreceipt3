package com.factoryreceipt.factoryreceipt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Wysyła e-mail na podstawie szablonu HTML.
     * W zależności od sklepu ładuje odpowiedni szablon, ustawia domyślny tytuł oraz nadawcę.
     *
     * @param to        adres odbiorcy
     * @param subject   tytuł wiadomości; jeśli null lub pusty, ustalany jest domyślnie
     * @param variables mapa zmiennych, które zastąpią placeholdery w szablonie
     * @param store     nazwa sklepu (np. "StockX" lub "MediaExpert")
     */
    public void sendStockxEmail(String to, String subject, Map<String, Object> variables, String store) {
        // Ustal domyślny tytuł
        if (subject == null || subject.isEmpty()) {
            if ("StockX".equalsIgnoreCase(store)) {
                subject = "Potwierdzenie zamówienia StockX";
            } else if ("MediaExpert".equalsIgnoreCase(store)) {
                subject = "Potwierdzenie zamówienia MediaExpert";
            } else {
                subject = "Potwierdzenie zamówienia";
            }
        }

        // Wybierz szablon w zależności od sklepu
        String templateName;
        if ("StockX".equalsIgnoreCase(store)) {
            templateName = "stockxTemplate";
        } else if ("MediaExpert".equalsIgnoreCase(store)) {
            templateName = "mediaexpertTemplate"; // Plik mediaexpertTemplate.html powinien być w src/main/resources/templates
        } else {
            templateName = "defaultTemplate";
        }

        try {
            // Wczytaj szablon HTML
            ClassPathResource resource = new ClassPathResource("templates/" + templateName + ".html");
            String template = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            // Zamień placeholdery (np. {{ orderNumber }}) na wartości z mapy
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String key = entry.getKey();
                // Jeśli wartość jest null, zamieniamy na pusty ciąg znaków
                String valueStr = (entry.getValue() == null) ? "" : entry.getValue().toString();

                String placeholder1 = "{{ " + key + " }}";
                String placeholder2 = "{{" + key + "}}";
                template = template.replace(placeholder1, valueStr);
                template = template.replace(placeholder2, valueStr);
            }

            // Utwórz wiadomość e-mail
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            // Ustawienie nadawcy w zależności od sklepu
            if ("StockX".equalsIgnoreCase(store)) {
                helper.setFrom("no-reply@stockx.com", "StockX");
            } else if ("MediaExpert".equalsIgnoreCase(store)) {
                helper.setFrom("no-reply@mediaexpert.pl", "MediaExpert");
            } else {
                helper.setFrom("no-reply@default.com", "Default Store");
            }

            helper.setText(template, true); // true – e-mail HTML

            mailSender.send(mimeMessage);
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
    }
}
