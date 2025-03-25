package backend.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import backend.model.Playlist;
import backend.model.User;
import backend.service.SpotifyService;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api")
public class HomeController {

    @Autowired
    private SpotifyService spotifyService;

    // TODO how to not hardcode these 
    private String authError = "http://localhost:4200/auth-error?error=";
    private String authSuccess = "http://localhost:4200/auth-success?data=";

    // @GetMapping("/")
    // public String home(Model model) { 

    //     model.addAttribute("authUrl", spotifyService.getAuthUrl());
    //     return "home";
    // }

    @ResponseBody
    @GetMapping("/spotify/auth/url")
    public String getAuthUrl() { 

        return spotifyService.getAuthUrl();

    }

    @ResponseBody
    @GetMapping("/callback")
    public void callback(
        @RequestParam(required = false) String code, 
        @RequestParam(required = false) String error, 
        HttpServletResponse response) throws IOException {

        // if (error != null) {
        //     // return ResponseEntity.badRequest().body("callback failure" + error);
        //     response.sendRedirect(authError + error);
        // }

        try { 
            User user = spotifyService.createUser(code); 
            spotifyService.saveUserToRepo(user);

            // Convert user object to JSON
            ObjectMapper mapper = new ObjectMapper();
            String userJson = mapper.writeValueAsString(user);
            // String encodedUserData = Base64.getEncoder().encodeToString(userJson.getBytes(StandardCharsets.UTF_8));
        
            // Redirect to frontend with the user data
            response.sendRedirect(authSuccess + user.getUsername());

        } catch (Exception e) { 
            response.sendRedirect(authError + e.getMessage());
        }

        // return ResponseEntity.ok().body(user);

    }

    ///
    /// 
    /// TODO abstract into spotify controller: 

    @ResponseBody
    @GetMapping("/recently-played")
    public String recentlyPlayed(
        @RequestParam String username) { 

        return spotifyService.getRecentlyPlayedSong(username);


    }

    // TODO is this a safe thing to do
    @ResponseBody
    @GetMapping("/access-token")
    public String accessToken(@RequestParam String username) { 

        return spotifyService.getValidAccessToken(username);
        
    }

    @ResponseBody
    @GetMapping("/search")
    public String searchForSong(@RequestParam String username, 
        String search) {

        // System.out.println("username: " + username);
        // System.out.println("search: " + search);

        return spotifyService.searchForSong(username, search);

    }

    @ResponseBody
    @PutMapping("/spotify/play")
    public void playSong(
        @RequestParam String username, 
        @RequestParam String deviceId, 
        @RequestParam String trackId) {

        System.out.println(">>>Device ID: " + deviceId);
        System.out.println(">>>Track ID: " + trackId);
        
        System.out.println(">>>Play Song Called in Backend");
        spotifyService.playSong(username, deviceId, trackId);

    }
    
}