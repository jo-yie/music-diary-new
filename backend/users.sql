CREATE TABLE users (

    user_id INT AUTO_INCREMENT, 
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    access_token VARCHAR(255), 
    refresh_token VARCHAR(255), 
    token_expiry BIGINT,
    CONSTRAINT pk_user_id PRIMARY KEY (user_id)

)