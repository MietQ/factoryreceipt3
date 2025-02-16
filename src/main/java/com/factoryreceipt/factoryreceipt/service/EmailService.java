package com.factoryreceipt.factoryreceipt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

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
     * Metoda wysyłająca e-mail na podstawie szablonu HTML, w którym występują placeholdery w formacie {{ variable }}.
     * W zależności od sklepu ładuje odpowiedni szablon, ustawia domyślny tytuł oraz nadawcę.
     *
     * @param to        adres odbiorcy
     * @param subject   tytuł wiadomości; jeśli null lub pusty, ustawiany jest domyślny w zależności od sklepu
     * @param variables mapa zmiennych do zamiany w szablonie
     * @param store     nazwa sklepu (np. "StockX")
     */
    public void sendStockxEmail(String to, String subject, Map<String, Object> variables, String store) {
        // Ustal domyślny tytuł, jeśli nie został podany
        if (subject == null || subject.isEmpty()) {
            if ("StockX".equalsIgnoreCase(store)) {
                subject = "Potwierdzenie zamówienia StockX";
            } else {
                subject = "Potwierdzenie zamówienia";
            }
        }

        // Wybierz szablon w zależności od sklepu
        String templateName;
        if ("StockX".equalsIgnoreCase(store)) {
            templateName = "stockxTemplate";
        } else {
            templateName = "defaultTemplate";
        }

        try {
            // Wczytaj szablon HTML z folderu resources/templates/
            ClassPathResource resource = new ClassPathResource("templates/" + templateName + ".html");
            String template = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            // Zamień placeholdery na wartości z mapy (przykład: {{ productName }})
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String placeholder1 = "{{ " + entry.getKey() + " }}";
                String placeholder2 = "{{" + entry.getKey() + "}}";
                template = template.replace(placeholder1, entry.getValue().toString());
                template = template.replace(placeholder2, entry.getValue().toString());
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            // Ustawienie nadawcy w zależności od sklepu
            if ("StockX".equalsIgnoreCase(store)) {
                helper.setFrom("no-reply@stockx.com", "StockX");
            } else {
                helper.setFrom("no-reply@default.com", "Default Store");
            }

            helper.setText(template, true); // true – wiadomość w formacie HTML

            mailSender.send(mimeMessage);
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
    }
}
