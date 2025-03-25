package backend.model;

public class User {

    private long userId; 
    private String username;
    private String email; 
    private String accessToken;
    private String refreshToken; 
    private Long tokenExpiry;

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public Long getTokenExpiry() {
        return tokenExpiry;
    }
    public void setTokenExpiry(Long tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
    
}
