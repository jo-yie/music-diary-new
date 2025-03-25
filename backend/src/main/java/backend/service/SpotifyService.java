package backend.service;

import java.io.StringReader;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.util.UriComponentsBuilder;
import backend.BackendApplication;
import backend.model.User;
import backend.repository.UserRepository;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

@Service
public class SpotifyService {

    // private final BackendApplication backendApplication;

    @Value("${spotify.client.id}")
    private String clientId; 

    @Value("${spotify.client.secret}")
    private String clientSecret; 

    @Value("${spotify.auth.url}")
    private String authUrl;

    @Value("${spotify.me.url}")
    private String meUrl; 

    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    @Value("${spotify.token.url}")
    private String tokenUrl; 

    @Value("${spotify.recently.played.url}")
    private String recentlyPlayedUrl;

    @Value("${spotify.search.url}")
    private String searchUrl;

    @Value("${spotify.play.url}")
    private String playUrl; 

    @Value("${spotify.scope}")
    private String scope;

    private final RestTemplate restTemplate = new RestTemplate(); 

    @Autowired 
    private UserRepository userRepository;

    // SpotifyService(BackendApplication backendApplication) {
    //     this.backendApplication = backendApplication;
    // } 

    // 1. generate auth url to get code
    public String getAuthUrl() { 

        // return UriComponentsBuilder.fromUriString(authUrl)
        //     .queryParam("client_id", clientId)
        //     .queryParam("response_type", "code")
        //     .queryParam("redirect_uri", redirectUri)
        //     .queryParam("scope", scope)
        //     .build()
        //     .toString();

        // public String getAuthUrl() { 
            return authUrl + "?client_id=" + clientId 
                   + "&response_type=code" 
                   + "&redirect_uri=" + redirectUri 
                   + "&scope=" + scope; // pass the scope as a space-separated list
        // }

    }

    // 2. exchange code for access token
    public JSONObject getAccessToken(String code) { 

        HttpHeaders headers = new HttpHeaders(); 
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>(); 
        requestBody.add("grant_type", "authorization_code"); 
        requestBody.add("code", code); 
        requestBody.add("redirect_uri", redirectUri); 

        HttpEntity<MultiValueMap<String, String>> requestEntity 
            = new HttpEntity<>(requestBody, headers);

        User user = new User(); 

        try { 
            ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl, 
                HttpMethod.POST, 
                requestEntity, 
                String.class);

            JSONObject jsonResponse = new JSONObject(response.getBody());

            return jsonResponse;
            
        } catch (Exception e) {
            // TODO error handling
            return new JSONObject(e.getMessage());
        }

    }

    // 3. get spotify username + email using access token
    public JSONObject getSpotifyDetails(String accessToken) { 

        HttpHeaders headers = new HttpHeaders(); 
        headers.set("Authorization", "Bearer " + accessToken); 

        HttpEntity<String> request = new HttpEntity<>("parameters", headers); 
        ResponseEntity<String> response = restTemplate.exchange(
            meUrl, 
            HttpMethod.GET, 
            request, 
            String.class);

        JSONObject jsonResponse = new JSONObject(response.getBody()); 

        return jsonResponse; 

    }

    // 4. create user object to save to repo 
    public User createUser(String code) { 

        User user = new User(); 

        JSONObject accessTokenResponse = getAccessToken(code); 
        String accessToken = accessTokenResponse.getString("access_token"); 
        String refreshToken = accessTokenResponse.getString("refresh_token");
        int expiresIn = accessTokenResponse.getInt("expires_in");

        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);

        long tokenExpiry = Instant.now().plusSeconds(expiresIn).toEpochMilli(); 
        user.setTokenExpiry(tokenExpiry);

        JSONObject spotifyDetails = getSpotifyDetails(accessToken);
        String username = spotifyDetails.getString("display_name");
        String email = spotifyDetails.getString("email"); 

        user.setUsername(username);
        user.setEmail(email);

        return user;

    }

    // 5. save user to repo 
    public void saveUserToRepo(User user) { 
        userRepository.save(user);

    }
    
    // get valid access token 
    public String getValidAccessToken(String username) { 

        Optional<User> userOpt = userRepository.findUserByUsername(username); 

        if (userOpt.isEmpty()) { 
            throw new RuntimeException("User not found"); 
        }

        User user = userOpt.get(); 

        long currentTime = Instant.now().toEpochMilli();

        // check if token expires in next 60s 
        if (user.getTokenExpiry() == null 
            || currentTime + 60000 > user.getTokenExpiry()) {

            refreshAccessToken(user);

            userOpt = userRepository.findUserByUsername(username); 
            if (userOpt.isEmpty()) {
                throw new RuntimeException("User not found after refresh");
            }

            user = userOpt.get();

        }

        return user.getAccessToken();

    }

    // refresh access token
    private void refreshAccessToken(User user) { 

        if (user.getRefreshToken() == null || user.getRefreshToken().isEmpty()) { 
            throw new RuntimeException("No refresh token available");

        }

        HttpHeaders headers = new HttpHeaders(); 
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String clientCredentials = clientId + ":" + clientSecret; 
        String encodedCredentials = Base64.getEncoder().encodeToString(clientCredentials.getBytes());
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>(); 
        requestBody.add("grant_type", "refresh_token"); 
        requestBody.add("refresh_token", user.getRefreshToken());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST, 
            requestEntity, 
            String.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JSONObject jsonResponse = new JSONObject(response.getBody());

            String newAccessToken = jsonResponse.getString("access_token"); 
            Integer expiresIn = jsonResponse.getInt("expires_in");

            long tokenExpiry = Instant.now().plusSeconds(expiresIn).toEpochMilli();

            user.setAccessToken(newAccessToken);
            user.setTokenExpiry(tokenExpiry);

            userRepository.updateAccessToken(user); 

        } else { 
            throw new RuntimeException("Failed to obtain refresh token: " + response.getStatusCode());
        }

    }

    // get recently played songs 
    public String getRecentlyPlayedSong(String username) {

        String validAccessToken = getValidAccessToken(username);

        HttpHeaders headers = new HttpHeaders(); 
        headers.set("Authorization", "Bearer " + validAccessToken);

        HttpEntity<String> request = new HttpEntity<>("parameters", headers); 
        ResponseEntity<String> response = restTemplate.exchange(
            recentlyPlayedUrl, 
            HttpMethod.GET, 
            request, 
            String.class
            );

        return response.getBody();

    }

    // search for song 
    public String searchForSong(String username, String search) {

        String updatedSearchUrl = UriComponentsBuilder.fromUriString(searchUrl)
            .queryParam("q", search)
            .queryParam("type", "track,album,artist")
            .queryParam("limit", 8)
            .build()
            .toString();

        HttpHeaders headers = new HttpHeaders(); 
        headers.setBearerAuth(getValidAccessToken(username)); // TODO dynamically insert username
        
        HttpEntity<String> request = new HttpEntity<>("parameters", headers);
        ResponseEntity<String> response = restTemplate.exchange(
            updatedSearchUrl,
            HttpMethod.GET,
            request,
            String.class); 

        // try (JsonReader reader = Json.createReader(new StringReader(response.getBody()))) {
        //     return reader.readObject().toString();
        // } 

        return response.getBody();

    } 

    public void playSong(String username, String deviceId, String trackId) {

        String endpointUrl = playUrl + "?device_id=" + deviceId;

        String trackUri = "spotify:track:" + trackId;

        HttpHeaders headers = new HttpHeaders(); 
        headers.setBearerAuth(getValidAccessToken(username));

        Map<String, Object> requestBody = new HashMap<>(); 
        requestBody.put("uris", Collections.singletonList(trackUri)); 

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        restTemplate.exchange(
            endpointUrl, 
            HttpMethod.PUT,
            request,
            String.class);

    }

}
