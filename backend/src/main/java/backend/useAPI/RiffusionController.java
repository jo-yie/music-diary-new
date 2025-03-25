package backend.useAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/riffusion")
public class RiffusionController {

    @Autowired
    private RiffusionService riffusionService; 

    @GetMapping("/test")
    public String getSong() { 
        return "riffusion";
    }

    @ResponseBody
    @GetMapping("/create-prompt")
    public String createPrompt(
        @RequestParam(required = true) String prompt, 
        @RequestParam(required = true) Boolean instrumental
    ) { 

        return riffusionService.createPrompt(prompt, instrumental);

    }

    @ResponseBody
    @GetMapping("/callback")
    public String callback() { 
        return "Generation complete";
    }
    
}
