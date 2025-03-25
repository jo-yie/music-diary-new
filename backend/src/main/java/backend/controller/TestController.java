package backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import backend.service.SpotifyService;

@Controller
public class TestController {

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/test/api/spotify/player")
    public String player(Model model) { 

        String accessToken = spotifyService.getValidAccessToken("mojojojothecheesepotato");
        model.addAttribute("accessToken", accessToken);

        System.out.println(accessToken);

        return "player";
    }
    
}
