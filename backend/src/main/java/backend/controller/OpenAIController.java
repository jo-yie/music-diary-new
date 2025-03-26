package backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.model.SaveSongsRequest;
import backend.service.PlaylistService;

@RestController
public class OpenAIController {

    @Autowired
    PlaylistService playlistService; 

    @GetMapping("/api/generate/lyrics")
    public ResponseEntity<Object> getLyrics(
        @RequestParam String username,
        @RequestParam String playlistId) {

        // get playlist 
        // feed playlist info to open ai 
        String s = playlistService.createLyrics(playlistId, username);
        
        return ResponseEntity.ok().body(s);

    }

    @PostMapping("/api/generate/songs")
    public ResponseEntity<Object> generateSong(
        @RequestParam String username, 
        @RequestParam String playlistId,
        @RequestBody String lyrics
        ) {

        String songPrompt = playlistService.createSongPrompt(playlistId, username);
        List<String> jobIds = playlistService.generateSong(songPrompt, lyrics);
        List<String> songUrls = playlistService.getSongUrls(jobIds);

        playlistService.saveSongs(username, playlistId, jobIds, lyrics, songUrls);

        return ResponseEntity.ok().body(songUrls);
        
    }

    @GetMapping("/api/get/songs")
    public ResponseEntity<Object> getSongs(
        @RequestParam String username, 
        @RequestParam String playlistId
    ) {

        List<String> songUrls = playlistService.getSongs(username, playlistId);

        return ResponseEntity.ok().body(songUrls);

    }

    @GetMapping("/api/get/lyrics")
    public ResponseEntity<Object> getMethodName(@RequestParam String username, @RequestParam String playlistId) {

        String lyrics = playlistService.getSongLyrics(username, playlistId); 

        return ResponseEntity.ok().body(lyrics);

    }
    
    
}
