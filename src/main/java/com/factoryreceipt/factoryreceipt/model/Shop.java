package com.factoryreceipt.factoryreceipt.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shops")
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String shopName;      // np. "MediaExpert", "StockX" itp.
    private String emailFrom;     // adres nadawcy e-maili (np. "no-reply@mediaexpert.pl")

    // NOWE pole – nazwa nadawcy, wyświetlana w e-mailu
    private String senderName;

    private String defaultSubject; // domyślny tytuł maila, np. "Twoje zamówienie"
    private String templateName;   // nazwa szablonu HTML (bez rozszerzenia .html)

    // Pole przechowujące konfigurację formularza (w formacie JSON)
    private String formConfig;

    // Gettery i settery
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getShopName() {
        return shopName;
    }
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
    public String getEmailFrom() {
        return emailFrom;
    }
    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }
    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public String getDefaultSubject() {
        return defaultSubject;
    }
    public void setDefaultSubject(String defaultSubject) {
        this.defaultSubject = defaultSubject;
    }
    public String getTemplateName() {
        return templateName;
    }
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    public String getFormConfig() {
        return formConfig;
    }
    public void setFormConfig(String formConfig) {
        this.formConfig = formConfig;
    }
}
