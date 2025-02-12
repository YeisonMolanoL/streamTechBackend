package com.TechPulseInnovations.streamTech.core.chat;
//import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.stereotype.Service;

@Service
public class ChatConnection {
    /*private final WebClient.Builder webClientBuilder;

    public ChatConnection(WebClient.Builder webClientBuilder){
        this.webClientBuilder = webClientBuilder;
    }

    public String sendMessage(String message, String number) {
        String url = "http://localhost:8000"; // Solo el host base
        System.out.println("Entro a enviar " + message + " " + number);
        WebClient webClient = webClientBuilder.baseUrl(url).build(); // Usa baseUrl aquí
        System.out.println("si paso el builder");

        try {
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/send-message") // Ruta relativa
                            .queryParam("message", message)
                            .queryParam("number", number)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // `block()` para realizar la operación de manera sincrónica

            System.out.println("Respuesta del servidor: " + response);
            return response;
        } catch (Exception e) {
            System.out.println("Graves error re peye del servidor: " + e.getMessage());
            return "Error en la solicitud: " + e.getMessage();
        }
    }*/
}
