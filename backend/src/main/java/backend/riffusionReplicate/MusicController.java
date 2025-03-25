package backend.riffusionReplicate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/music")
public class MusicController {

    @Autowired
    private ReplicateRiffusionService replicateRiffusionService; 

    // Show the form for music generation
    @GetMapping
    public String showMusicForm(Model model) {
        if (!model.containsAttribute("prompt")) {
            model.addAttribute("prompt", "");
        }
        return "music-generator";
    }

    // Handle the form submission
    @PostMapping("/generate")
    public String generateMusic(@RequestParam String prompt, RedirectAttributes redirectAttributes) {
        // Store the prompt in flash attributes to preserve it after redirect
        redirectAttributes.addFlashAttribute("prompt", prompt);
        
        // Generate a unique ID for this audio file
        String audioId = UUID.randomUUID().toString();
        redirectAttributes.addFlashAttribute("audioId", audioId);
        
        // Set a flag indicating that music is being generated
        redirectAttributes.addFlashAttribute("generating", true);
        
        // Start music generation in a separate thread to not block the UI
        new Thread(() -> {
            try {
                // Generate the music
                Resource audioResource = replicateRiffusionService.generateMusic(prompt);
                
                // Store the resource in application context or a cache service
                // For simplicity, we're using a static cache here
                AudioCache.storeAudio(audioId, audioResource);
            } catch (Exception e) {
                // Log error - in a real application you would handle this better
                e.printStackTrace();
                AudioCache.storeError(audioId, e.getMessage());
            }
        }).start();
        
        return "redirect:/music/status?id=" + audioId;
    }
    
    // Check status of music generation
    @GetMapping("/status")
    public String checkStatus(@RequestParam String id, Model model) {
        model.addAttribute("audioId", id);
        
        if (AudioCache.isReady(id)) {
            model.addAttribute("ready", true);
            model.addAttribute("error", AudioCache.getError(id));
        } else {
            model.addAttribute("ready", false);
        }
        
        return "music-status";
    }

    // Serve the generated audio file
    @GetMapping("/play/{id}")
    public ResponseEntity<Resource> getAudio(@PathVariable String id) {
        Resource audioResource = AudioCache.getAudio(id);
        
        if (audioResource == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header(HttpHeaders.CONTENT_TYPE, "audio/wav")
                .body(audioResource);
    }
}

// A simple static cache for audio resources
// In a real application, you would use a proper caching solution
class AudioCache {
    private static final Map<String, Resource> audioCache = new HashMap<>();
    private static final Map<String, String> errorCache = new HashMap<>();
    
    public static void storeAudio(String id, Resource audio) {
        audioCache.put(id, audio);
    }
    
    public static void storeError(String id, String error) {
        errorCache.put(id, error);
    }
    
    public static Resource getAudio(String id) {
        return audioCache.get(id);
    }
    
    public static String getError(String id) {
        return errorCache.get(id);
    }
    
    public static boolean isReady(String id) {
        return audioCache.containsKey(id) || errorCache.containsKey(id);
    }
    
}
