package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ChatGPTConsole {
    private static final RateLimiter rateLimiter = new RateLimiter(5, 1000); // Permite até 5 solicitações por segundo
    
    public static void main(String[] args) throws IOException {
        // Se o RateLimiter permitir a solicitação, envie a solicitação para a API do OpenAI
        if (rateLimiter.allowRequest()) {
            String endpoint = "https://api.openai.com/v1/chat/completions";
            String apiKey = "SUA_API_KEY_AQUI";
            String message = "Como posso te ajudar?";
            String response = sendRequest(endpoint, apiKey, message);
            System.out.println(response);
        } else {
            System.out.println("Limite de solicitações excedido. Por favor, tente novamente mais tarde.");
        }
    }
    
    public static String sendRequest(String endpoint, String apiKey, String message) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + apiKey);
        con.setDoOutput(true);
        String jsonInputString = "{\"model\": \"gpt-3.5-turbo\", \"messages\": [{\"role\": \"system\", \"content\": \"" + message + "\"}]}";
        con.getOutputStream().write(jsonInputString.getBytes(StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
    
    static class RateLimiter {
        private final int requestLimit; // Limite de solicitações
        private final long timeWindow; // Janela de tempo em milissegundos
        private int requestCount; // Contador de solicitações
        private long windowStart; // Hora de início da janela de tempo
        
        public RateLimiter(int requestLimit, long timeWindow) {
            this.requestLimit = requestLimit;
            this.timeWindow = timeWindow;
            this.requestCount = 0;
            this.windowStart = Instant.now().toEpochMilli();
        }
        
        public synchronized boolean allowRequest() {
            long currentTime = Instant.now().toEpochMilli();
            if (currentTime - windowStart >= timeWindow) {
                // Se a janela de tempo passou, redefina o contador e o tempo de início da janela
                requestCount = 0;
                windowStart = currentTime;
            }
            if (requestCount < requestLimit) {
                // Se o limite de solicitações não foi atingido, permita a solicitação e atualize o contador
                requestCount++;
                return true;
            }
            return false; // Caso contrário, não permita a solicitação
        }
    }
}
