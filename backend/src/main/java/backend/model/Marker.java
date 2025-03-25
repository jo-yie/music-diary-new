package backend.model;

public class Marker {

    private String id; 
    private String title; 
    private String message; 
    private MarkerPosition position;
    private Song song;
    public Marker() {
    }
    public Marker(String id, String title, String message, MarkerPosition position, Song song) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.position = position;
        this.song = song;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public MarkerPosition getPosition() {
        return position;
    }
    public void setPosition(MarkerPosition position) {
        this.position = position;
    }
    public Song getSong() {
        return song;
    }
    public void setSong(Song song) {
        this.song = song;
    }
    @Override
    public String toString() {
        return "Marker [id=" + id + ", title=" + title + ", message=" + message + ", position=" + position + ", song="
                + song + "]";
    }
    
    public String toStringPrompt() { 
        return "Marker ID: " + id + ", "
            + "Marker Title: " + title + ", "
            + "Marker Message: " + message + ", "
            + "Marker Lat: " + position.getLat() + ", "
            + "Marker Long: " + position.getLng() + ". ";
    }
    
}
