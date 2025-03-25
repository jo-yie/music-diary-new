package backend.riffusionReplicate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ReplicateRiffusionService {

    private final RestTemplate restTemplate = new RestTemplate(); 

    @Value("${replicate.api.token}")
    private String replicateApiToken;

    // private final RestTemplate restTemplate;
    // private final String replicateApiToken;

    // public ReplicateRiffusionService(
    //         RestTemplate restTemplate,
    //         @Value("${replicate.api.token}") String replicateApiToken) {
    //     this.restTemplate = restTemplate;
    //     this.replicateApiToken = replicateApiToken;
    // }

    public Resource generateMusic(String prompt) {
        // Replicate API endpoint
        String replicateUrl = "https://api.replicate.com/v1/predictions";
        
        // Create request body for Replicate
        Map<String, Object> input = new HashMap<>();
        input.put("prompt_a", prompt);
        
        Map<String, Object> requestBody = new HashMap<>();
        // requestBody.put("version", "823b0c800920b91c7c6543e6a3b18800de91ddb0c0e1416c227b96e60ca68988");
        requestBody.put("version", "8cf61ea6c56afd61d8f5b9ffd14d7c216c0a93844ce2d82ac1c9ecc9c7f24e05");
        requestBody.put("input", input);
        
        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + replicateApiToken);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        // Make initial request to start the prediction
        ResponseEntity<Map> createResponse = restTemplate.exchange(
                replicateUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );
        
        // Get the prediction ID
        String predictionId = (String) createResponse.getBody().get("id");
        String getUrl = replicateUrl + "/" + predictionId;
        
        // Poll until the prediction is complete
        Map<String, Object> prediction;
        String status;
        do {
            // Wait before polling again
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Check prediction status
            HttpEntity<Void> getEntity = new HttpEntity<>(headers);
            ResponseEntity<Map> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    getEntity,
                    Map.class
            );
            
            prediction = getResponse.getBody();
            status = (String) prediction.get("status");
            
        } while ("starting".equals(status) || "processing".equals(status));
        
        // Check if the prediction succeeded
        if ("succeeded".equals(status)) {
            // Get the audio URL from the output
            Map<String, Object> output = (Map<String, Object>) prediction.get("output");
            String audioUrl = (String) output.get("audio");
            
            // Download the audio file
            ResponseEntity<byte[]> audioResponse = restTemplate.getForEntity(audioUrl, byte[].class);
            byte[] audioBytes = audioResponse.getBody();
            
            // Return as a resource
            return new ByteArrayResource(audioBytes);
        } else {
            throw new RuntimeException("Failed to generate audio: " + prediction.get("error"));
        }
    }
}
