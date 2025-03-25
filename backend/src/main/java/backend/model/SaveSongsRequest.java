package backend.model;

import java.util.List;

public class SaveSongsRequest {

    private String lyrics; 
    private List<String> songUrls;
    
    public SaveSongsRequest() {
    }
    
    public SaveSongsRequest(String lyrics, List<String> songUrls) {
        this.lyrics = lyrics;
        this.songUrls = songUrls;
    }

    public String getLyrics() {
        return lyrics;
    }
    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }
    public List<String> getSongUrls() {
        return songUrls;
    }
    public void setSongUrls(List<String> songUrls) {
        this.songUrls = songUrls;
    } 
    
}
