package backend.useAPI;

import javax.print.attribute.standard.Media;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import backend.model.User;

@Service
public class RiffusionService {

    @Value("${useapi.api.token}")
    private String useApiToken;

    // TODO put in app properties
    private String useApiCreatePromptUrl = 
        "https://api.useapi.net/v1/riffusion/music/create-prompt";

    private RestTemplate restTemplate = new RestTemplate(); 

    // for reference: https://useapi.net/docs/api-riffusion-v1/post-riffusion-music-create-prompt
    public String createPrompt(
        String prompt, 
        Boolean instrumental) { 

        HttpHeaders headers = new HttpHeaders(); 
        headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.set("Authorization", "Bearer " + useApiToken);
        headers.setBearerAuth(useApiToken);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>(); 
        requestBody.add("prompt", prompt); 
        requestBody.add("instrumental", String.valueOf(instrumental));

        HttpEntity<MultiValueMap<String, String>> requestEntity = 
            new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            useApiCreatePromptUrl,
            HttpMethod.POST, 
            requestEntity, 
            String.class);

        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray jobs = jsonResponse.getJSONArray("jobs");

        return jobs.getJSONObject(0).getString("id") + "\n" + 
            jobs.getJSONObject(1).getString("id");

        // try { 
        //     ResponseEntity<String> response = restTemplate.exchange(
        //         tokenUrl, 
        //         HttpMethod.POST, 
        //         requestEntity, 
        //         String.class);

        //     JSONObject jsonResponse = new JSONObject(response.getBody());

        //     return jsonResponse;
            
        // } catch (Exception e) {
        //     // TODO error handling
        //     return new JSONObject(e.getMessage());
        // }

    }
    
}
