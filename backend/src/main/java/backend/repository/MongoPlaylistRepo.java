package backend.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import backend.model.Playlist;

@Repository
public class MongoPlaylistRepo {

    @Autowired
    private MongoTemplate template;

    // db.playlists.insertOne({
    //     _id: "7bfe87b8",
    //     playlistId: "7bfe87b8",
    //     playlistName: "playlist",
    //     username: "username",
    //     markers: [
    //         {
    //             id: "87e20b18",
    //             title: "title 1",
    //             message: "message 1",
    //             position: {
    //                 lat: 1.3774670895054397,
    //                 lng: 103.96974782498951
    //             },
    //             options: {
    //                 draggable: false
    //             },
    //             song: {
    //                 trackName: "Bags",
    //                 artist: ["Clairo"],
    //                 spotifyId: "6UFivO2zqqPFPoQYsEMuCc",
    //                 imageUrl: "https://i.scdn.co/image/ab67616d0000b27333ccb60f9b2785ef691b2fbc"
    //             }
    //         }
    //     ]
    // });
    
    public void savePlaylistDetails(Playlist playlist, String username) { 

        Document doc = new Document(); 
        doc.put("_id", playlist.getPlaylistId());
        doc.put("playlist_id", playlist.getPlaylistId());
        doc.put("playlist_name", playlist.getPlaylistName());
        doc.put("playlist_description", playlist.getPlaylistDescription());
        doc.put("username", username);
        doc.put("markers", playlist.getMarkers());

        template.insert(doc, "playlists");

    }
    
    // db.playlists.findOne(
    //     { _id: "baf65292", username: "mojojojothecheesepotato" },
    //     { _id : 0, markers : 1 }
    // )

    public Optional<Document> getPlaylist(String playlistId, String username) {
        
        Criteria criteria = new Criteria()
            .where("_id").is(playlistId)
            .and("username").is(username);

        Query query = new Query().addCriteria(criteria); 
        // query.fields().exclude("_id").include("markers");

        Document document = template.findOne(
            query, Document.class, "playlists");

        return Optional.of(document);

    }

    // db.songs.insertOne({
    //     _id: ,
    //     job_id: ,
    //     playlist_id: ,
    //     username: ,
    //     lyrics: ,
    //     song_url: 
    // })

    public void saveSong(
        String username, 
        String playlistId, 
        String jobId, 
        String lyrics, 
        String songUrl) {

        Document doc = new Document(); 
        doc.put("_id", jobId);
        doc.put("job_id", jobId); 
        doc.put("playlist_id", playlistId); 
        doc.put("username", username); 
        doc.put("lyrics", lyrics); 
        doc.put("song_url", songUrl);

        template.insert(doc, "songs");

    }

    // db.songs.find(
    //     { playlist_id: "6bdaf62b", username: "mojojojothecheesepotato" },
    //     { _id: 0, song_url: 1}
    // )

    public Optional<List<Document>> getSongs(String playlistId, String username) {

        Criteria criteria = new Criteria()
            .where("playlist_id").is(playlistId)
            .and("username").is(username);

        Query query = new Query().addCriteria(criteria);
        query.fields().exclude("_id").include("song_url");

        List<Document> documents = template.find(
            query, 
            Document.class, 
            "songs");

        return Optional.of(documents);

    }

}
