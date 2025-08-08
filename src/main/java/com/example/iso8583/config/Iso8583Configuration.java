package com.example.iso8583.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração Spring para propriedades ISO 8583.
 * Demonstra como configurar o sistema sem usar XML.
 */
@Configuration
@ConfigurationProperties(prefix = "app.iso8583")
public class Iso8583Configuration {

    private boolean assignDate = true;
    private String encoding = "UTF-8";
    private boolean useBinaryMessages = false;
    private boolean ignoreLastMissingField = true;

    // Getters e Setters
    public boolean isAssignDate() {
        return assignDate;
    }

    public void setAssignDate(boolean assignDate) {
        this.assignDate = assignDate;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public boolean isUseBinaryMessages() {
        return useBinaryMessages;
    }

    public void setUseBinaryMessages(boolean useBinaryMessages) {
        this.useBinaryMessages = useBinaryMessages;
    }

    public boolean isIgnoreLastMissingField() {
        return ignoreLastMissingField;
    }

    public void setIgnoreLastMissingField(boolean ignoreLastMissingField) {
        this.ignoreLastMissingField = ignoreLastMissingField;
    }

    @Override
    public String toString() {
        return "Iso8583Configuration{" +
                "assignDate=" + assignDate +
                ", encoding='" + encoding + '\'' +
                ", useBinaryMessages=" + useBinaryMessages +
                ", ignoreLastMissingField=" + ignoreLastMissingField +
                '}';
    }
}
