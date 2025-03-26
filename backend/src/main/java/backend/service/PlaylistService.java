package backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.print.attribute.standard.Media;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import backend.model.Marker;
import backend.model.MarkerPosition;
import backend.model.Playlist;
import backend.model.Song;
import backend.repository.MongoPlaylistRepo;
import backend.repository.MySqlPlaylistRepo;

@Service
public class PlaylistService {

    @Autowired
    private MySqlPlaylistRepo mySqlPlaylistRepo; 

    @Autowired
    private MongoPlaylistRepo mongoPlaylistRepo;

    @Transactional
    public void _insertPlaylists(Playlist playlist, String username) {
        mySqlPlaylistRepo.savePlaylistInfo(playlist, username);
        mongoPlaylistRepo.savePlaylistDetails(playlist, username);
    }

    // insert playlist info into sql
    public void insertPlaylistInfo(Playlist playlist, String username) { 

        mySqlPlaylistRepo.savePlaylistInfo(playlist, username);

    }

    // insert playlist details into mongo
    public void insertPlaylistDetails(Playlist playlist, String username) {

        mongoPlaylistRepo.savePlaylistDetails(playlist, username);

    }
    
    // get playlist 
    public Optional<Playlist> getPlaylist(String playlistId, String username) {

        if (mySqlPlaylistRepo.checkPlaylistInfo(playlistId, username)) {

            Optional<Document> opt = mongoPlaylistRepo.getPlaylist(playlistId, username);

            if (opt.isPresent()) {
                Document doc = opt.get(); 
                Playlist p = docToPlaylistPojo(doc);
                return Optional.of(p);

            }

        }

        return Optional.empty();

    }

    private Playlist docToPlaylistPojo(Document doc) {

        Playlist p = new Playlist(); 
        p.setPlaylistId(doc.getString("playlist_id"));
        p.setPlaylistName(doc.getString("playlist_name"));
        p.setPlaylistDescription(doc.getString("playlist_description"));

        List<Document> markersDocs = doc.getList("markers", Document.class);
        List<Marker> markers = new ArrayList<>(); 
        for (Document markerDoc: markersDocs) {
            markers.add(docToMarkerPojo(markerDoc));
        }
        p.setMarkers(markers);

        return p;

    }

    private Marker docToMarkerPojo(Document doc) {

        Marker m = new Marker(); 
        m.setId(doc.getString("_id"));
        m.setTitle(doc.getString("title"));
        m.setMessage(doc.getString("message"));

        // extract pos 
        Document posDoc = doc.get("position", Document.class);
        MarkerPosition pos = new MarkerPosition(
            posDoc.getDouble("lat"),
            posDoc.getDouble("lng")
        );
        m.setPosition(pos);

        // extract song 
        Document songDoc = doc.get("song", Document.class);
        Song s = new Song(); 
        s.setSpotifyId(songDoc.getString("spotifyId"));
        s.setImageUrl(songDoc.getString("imageUrl"));
        s.setTrackName(songDoc.getString("trackName"));
        s.setArtist(songDoc.getList("artist", String.class));
        m.setSong(s);

        return m;

    }

    // get all playlists 
    public List<Playlist> getAlPlaylists(String username) {

        List<String> playlistIds = getPlaylistIds(username);
        List<Playlist> playlists = new ArrayList<>();

        for (String playlistId: playlistIds) {
            playlists.add(getPlaylist(playlistId, username).get());
        }

        return playlists;

    }

    // get all playlist ids 
    private List<String> getPlaylistIds(String username) {

        return mySqlPlaylistRepo.getPlaylistIds(username);

    }




    // GENERATE LYRICS

    @Value("${open.ai.api.key}")
    private String openAiApiKey;

    private String openAiEndpoint = "https://api.openai.com/v1/chat/completions";
    private RestTemplate restTemplate = new RestTemplate();

    public String createLyrics(String playlistId, String username) {

        Playlist p = getPlaylist(playlistId, username).get();
        String prompt = buildLyricsPrompt(p);
        return generateLyrics(prompt);

    }

    private String buildLyricsPrompt(Playlist p) {

        String prompt = "Write the lyrics to a song that is associated with these messages and the locations of or close to the longitude/latitudes. Make explicit references to the locations (not the coordinates) and the associated message and/or title. Respond only with the lyrics, not the title. Do not include any text styling in your response.";

        String markers = "";

        for (Marker m: p.getMarkers()) {
            markers += m.toStringPrompt();
        }

        return prompt + markers;

    }

    private String generateLyrics(String prompt) {

        Map<String, Object> requestBody = new HashMap<>(); 

        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", "You are a songwriter that generates lyrics"),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders(); 
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = 
            new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            openAiEndpoint, 
            HttpMethod.POST, 
            request, 
            String.class);

        // Parse JSON response correctly
        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray choicesArray = jsonObject.getJSONArray("choices");

        if (choicesArray.length() > 0) {
            JSONObject messageObject = choicesArray.getJSONObject(0).getJSONObject("message");
            return messageObject.getString("content");
        } else {
            return "No lyrics generated.";
        }

    }


    // GENERATE SONG PROMPT

    public String createSongPrompt(String playlistId, String username) {

        Playlist p = getPlaylist(playlistId, username).get();
        // call build prompt
        String prompt = buildSongPrompt(p);
        // call generate prompt
        return generateSongPrompt(prompt);

    }

    private String buildSongPrompt(Playlist p) {

        String prompt = "Generate a prompt for an AI song generator inspired by the genres and styles of the following songs. Do not include song titles or artist names in your response. Limit your response to 50 words. Respond only with the prompt. Do not include any text styling in your response. ";

        String songs = "";

        for (Marker m: p.getMarkers()) {
            songs += m.getSong().toStringSong();
        }

        return prompt + songs;

    }

    private String generateSongPrompt(String prompt) {

        Map<String, Object> requestBody = new HashMap<>(); 

        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", "You are a prompt engineer generating prompts to create songs"),
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders(); 
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = 
            new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            openAiEndpoint, 
            HttpMethod.POST, 
            request, 
            String.class);

        // Parse JSON response correctly
        JSONObject jsonObject = new JSONObject(response.getBody());
        JSONArray choicesArray = jsonObject.getJSONArray("choices");

        if (choicesArray.length() > 0) {
            JSONObject messageObject = choicesArray.getJSONObject(0).getJSONObject("message");
            return messageObject.getString("content");
        } else {
            return "No song prompt generated.";
        }

    } 


    // GENERATE SONG 

    @Value("${useapi.api.token}")
    private String useApiToken;

    private String createSongEndpoint = "https://api.useapi.net/v1/riffusion/music/create-compose";

    public List<String> generateSong(String prompt, String lyrics) {

        Map<String, String> requestBody = new HashMap<>(); 
        requestBody.put("prompt_1", prompt);
        requestBody.put("lyrics", lyrics);

        HttpHeaders headers = new HttpHeaders(); 
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(useApiToken);
        
        HttpEntity<Map<String, String>> request = 
            new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            createSongEndpoint, 
            HttpMethod.POST,
            request,
            String.class);

        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray jsonArray = jsonResponse.getJSONArray("jobs");

        List<String> jobIds = new ArrayList<>(); 

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject job = jsonArray.getJSONObject(i);
            jobIds.add(job.getString("id"));
        }

        System.out.println(">>>Job ID 1: " + jobIds.get(0));
        System.out.println(">>>Job ID 2: " + jobIds.get(1));
    
        // TODO return empty list ?? 
        if (jobIds.isEmpty()) {
            return new ArrayList<>(); 
        }

        return jobIds;

    }


    // GET SONG URL FROM JOB IDS 

    // https://api.useapi.net/v1/riffusion/music/?id=383c3743-e766-4292-884b-63388617351c
    private String getSongEndpoint = "https://api.useapi.net/v1/riffusion/music/?id=";

    public List<String> getSongUrls(List<String> jobIds) {

        List<String> songUrls = new ArrayList<>(); 

        for (String jobId: jobIds) {
            String songUrl = getSongUrl(jobId);
            songUrls.add(songUrl);
            System.out.println(">>>Song Url: " + songUrl);
        }

        return songUrls;

    }

    private String getSongUrl(String jobId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(useApiToken);
        String urlEndpoint = getSongEndpoint + jobId;
        HttpEntity<String> request = new HttpEntity<>(headers);
    
        int maxRetries = 5;
        for (int i = 0; i < maxRetries; i++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                    urlEndpoint,
                    HttpMethod.GET,
                    request,
                    String.class);
    
                System.out.println(">>>API Response for " + jobId + ": " + response.getBody());
    
                if (response.getStatusCode() == HttpStatus.OK) {
                    JSONArray jsonArray = new JSONArray(response.getBody());
                    if (jsonArray.length() > 0) {
                        return jsonArray.getJSONObject(0).getString("download_url");
                    }
                }
    
                Thread.sleep(3000); // Wait before retrying
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "Song not found after retries";
    }

    // playlistService.saveSongs(username, playlistId, jobIds, lyrics, songUrls);
    public void saveSongs(
        String username, 
        String playlistId, 
        List<String> jobIds, 
        String lyrics, 
        List<String> songUrls) { 


        for (int i = 0; i < 2; i++) {

            mongoPlaylistRepo.saveSong(
                username, 
                playlistId, 
                jobIds.get(i), 
                lyrics, 
                songUrls.get(i));

        }

    }

    public List<String> getSongs(String username, String playlistId) {

        List<String> songUrls = new ArrayList<>();
        Optional<List<Document>> optDocs = mongoPlaylistRepo.getSongs(playlistId, username);

        if (optDocs.isPresent()) {

            List<Document> docs = optDocs.get(); 

            for (Document d: docs) {
                songUrls.add(d.getString("song_url"));

            }

        }

        return songUrls;

    }
    
    public String getSongLyrics(String username, String playlistId) {

        return mongoPlaylistRepo.getSongLyrics(playlistId, username);

    }

    // get songs from mongodb 
    // if empty --> show generate song component

}
