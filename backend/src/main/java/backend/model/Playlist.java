package backend.model;

import java.util.List;

public class Playlist {

    private String playlistId; 
    private String playlistName; 
    private String playlistDescription;
    private List<Marker> markers;
    public Playlist() {
    }
    public Playlist(String playlistId, String playlistName, String playlistDescription, List<Marker> markers) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistDescription = playlistDescription;
        this.markers = markers;
    }
    public String getPlaylistId() {
        return playlistId;
    }
    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }
    public String getPlaylistName() {
        return playlistName;
    }
    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }
    public String getPlaylistDescription() {
        return playlistDescription;
    }
    public void setPlaylistDescription(String playlistDescription) {
        this.playlistDescription = playlistDescription;
    }
    public List<Marker> getMarkers() {
        return markers;
    }
    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }
    
}
