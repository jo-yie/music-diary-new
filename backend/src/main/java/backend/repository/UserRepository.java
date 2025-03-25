package backend.repository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import backend.model.User;

@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate; 

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User(); 
        user.setUserId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setAccessToken(rs.getString("access_token"));
        user.setRefreshToken(rs.getString("refresh_token"));
        user.setTokenExpiry(rs.getLong("token_expiry"));
        return user;
    };

    public Optional<User> findUserByUsername(String username) { 

        String sqlQuery = 
        """
            SELECT * FROM users
                WHERE username = ?        
        """;

        try { 
            User user = jdbcTemplate.queryForObject(
                sqlQuery, 
                userRowMapper, 
                username);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty(); 
        }

    }

    public void save(User user) {

        Optional<User> existingUser = findUserByUsername(user.getUsername());

        if (existingUser.isPresent()) {
            String updateQuery = 
            """
                UPDATE users 
                    SET email = ?, access_token = ?, refresh_token = ?, token_expiry = ?
                    WHERE username = ?        
            """;

            jdbcTemplate.update(
                updateQuery, 
                user.getEmail(), 
                user.getAccessToken(), 
                user.getRefreshToken(),
                user.getTokenExpiry(), 
                user.getUsername());

        } else { 

            String insertQuery = 
            """
                INSERT INTO users (username, email, access_token, refresh_token, token_expiry) 
                    VALUES (?, ?, ?, ?, ?)       
            """;
    
            jdbcTemplate.update(
                insertQuery, 
                user.getUsername(), 
                user.getEmail(), 
                user.getAccessToken(), 
                user.getRefreshToken(),
                user.getTokenExpiry());

        }

    }

    // TODO check that this works
    public void updateAccessToken(User user) {

        String sqlQuery = 
        """
            UPDATE users 
            SET access_token = ?, 
                token_expiry = ?
            WHERE username = ?        
        """;

        jdbcTemplate.update(
            sqlQuery, 
            user.getAccessToken(), 
            user.getTokenExpiry(), 
            user.getUsername());

    }
    
}
