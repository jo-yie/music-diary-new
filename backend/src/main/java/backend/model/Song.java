package backend.model;

import java.util.Arrays;
import java.util.List;

public class Song {
    private String spotifyId; 
    private String imageUrl; 
    private String trackName; 
    private List<String> artist;
    public Song() {
    }
    public Song(String spotifyId, String imageUrl, String trackName, List<String> artist) {
        this.spotifyId = spotifyId;
        this.imageUrl = imageUrl;
        this.trackName = trackName;
        this.artist = artist;
    }
    public String getSpotifyId() {
        return spotifyId;
    }
    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getTrackName() {
        return trackName;
    }
    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
    public List<String> getArtist() {
        return artist;
    }
    public void setArtist(List<String> artist) {
        this.artist = artist;
    }
    @Override
    public String toString() {
        return "Song [spotifyId=" + spotifyId + ", imageUrl=" + imageUrl + ", trackName=" + trackName + ", artist="
                + artist + "]";
    }

    public String toStringSong() { 
        return trackName + " by " + artist.toString() + ". ";
    }
    
}
