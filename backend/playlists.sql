-- use database 
USE final_project;

CREATE TABLE playlists (

    playlist_id VARCHAR(8) NOT NULL, -- primary key 
    playlist_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
    username VARCHAR(255) NOT NULL, -- foreign key
    CONSTRAINT pk_playlist_id PRIMARY KEY (playlist_id), 
    CONSTRAINT fk_username
        FOREIGN KEY (username)
        REFERENCES users(username)

);