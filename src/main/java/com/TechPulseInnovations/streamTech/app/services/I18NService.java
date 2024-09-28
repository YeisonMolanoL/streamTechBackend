package com.TechPulseInnovations.streamTech.app.services;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class I18NService {
    private final MessageSource messageSource;

    public I18NService(MessageSource messageSource){
        this.messageSource = messageSource;
    }

    public String getMessage(String key) {
        return getMessage(key, null, Locale.getDefault());
    }

    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, Locale.getDefault());
    }

    public String getMessage(String key, String arg) {
        return messageSource.getMessage(key, new Object[]{arg}, Locale.getDefault());
    }

    public String getMessage(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }
}
