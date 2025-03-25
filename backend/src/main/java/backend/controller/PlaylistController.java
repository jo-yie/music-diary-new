package backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.model.Playlist;
import backend.service.PlaylistService;

@RestController
@RequestMapping("/api")
public class PlaylistController {

    @Autowired
    private PlaylistService playlistService; 
    
    @PostMapping("/save/playlist")
    public ResponseEntity<Object> savePlaylist(@RequestParam String username, 
        @RequestBody Playlist playlist) {

        System.out.println(">>>Username: " + username);
        System.out.println(">>>Payload: " + playlist.toString());

        try { 
            playlistService._insertPlaylists(playlist, username);

            return ResponseEntity
                .ok()
                .body(Map.of(">>>Success", "Inserted into both dbs"));

        } catch (Exception e) {

            return ResponseEntity
                .badRequest()
                .body(Map.of(">>>Error", e.getMessage()));

        }

    }

    @GetMapping("/get/playlist")
    public ResponseEntity<Object> getPlaylist(
        @RequestParam String username, @RequestParam String playlistId) {

        System.out.println(">>>Username: " + username);
        System.out.println(">>>Playlist ID: " + playlistId);

        // Playlist p = new Playlist();
        // p.setPlaylistId(playlistId);
        // p.setPlaylistName("whoppe");

        Optional<Playlist> playlistOpt = playlistService.getPlaylist(playlistId, username);

        try { 

            if (playlistOpt.isPresent()) {
                Playlist p = playlistOpt.get();
                return ResponseEntity.ok().body(p);

            } else { 
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("Error", "Playlist doesn't exist"));

            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));
        }
    }

    @GetMapping("/playlists")
    public ResponseEntity<Object> getAllPlaylists(@RequestParam String username) {

        try {

            // TODO check what happens if this list is empty 
            List<Playlist> playlists = playlistService.getAlPlaylists(username);

            return ResponseEntity.ok()
                .body(playlists);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("Error", e.getMessage()));

        }

    }

}
