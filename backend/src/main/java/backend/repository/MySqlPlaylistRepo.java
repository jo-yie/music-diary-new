package backend.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.RowSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import backend.model.Playlist;

@Repository
public class MySqlPlaylistRepo {

    @Autowired
    private JdbcTemplate template; 

    private String SQL_INSERT_PLAYLIST = 
    """
        INSERT INTO playlists (playlist_id, playlist_name, username)
            VALUES (?, ?, ?)        
    """;

    public void savePlaylistInfo(Playlist playlist, String username) { 

        template.update(
            SQL_INSERT_PLAYLIST, 
            playlist.getPlaylistId(),
            playlist.getPlaylistName(), 
            username
            );

    }

    private String SQL_CHECK_PLAYLIST = 
    """
        SELECT COUNT(*) AS COUNT FROM playlists
            WHERE  playlist_id = ?
            AND username = ?
    """;

    public boolean checkPlaylistInfo(String playlistId, String username) {

        return template.queryForObject(
            SQL_CHECK_PLAYLIST, 
            Integer.class,
            playlistId, 
            username) == 1;

    }

    // get all playlist id 
    private String SQL_PLAYLIST_IDS = 
    """
        SELECT playlist_id FROM playlists
            WHERE username = ?        
    """;

    public List<String> getPlaylistIds(String username) {

        SqlRowSet rs = template.queryForRowSet(
            SQL_PLAYLIST_IDS,
            username
        );

        List<String> playlistIds = new ArrayList<>(); 

        while (rs.next()) {
            String playlistId = rs.getString("playlist_id");
            playlistIds.add(playlistId);
        }

        return playlistIds; 

    }
    
}
