CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    score INT DEFAULT 0,
    roles ENUM('USER', 'ADMIN', 'ORGANIZER', 'REVIEWER') NOT NULL,
    profile_picture VARCHAR(2083),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE location (
    location_id SERIAL PRIMARY KEY,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL
);

CREATE TABLE hunt (
    hunt_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    organizer_id  BIGINT UNSIGNED NOT NULL,
    winner_id BIGINT UNSIGNED,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status  ENUM('DRAFT', 'LIVE', 'FINISHED', 'TERMINATED') NOT NULL,
    hunt_img_uri VARCHAR(2083),
    location_id BIGINT UNSIGNED NOT NULL,
    FOREIGN KEY (organizer_id) REFERENCES users(user_id),
    FOREIGN KEY (winner_id) REFERENCES users(user_id),
    FOREIGN KEY (location_id) REFERENCES location(location_id)
);

CREATE TABLE challenge_type (
    challenge_type_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT
);

CREATE TABLE challenge (
    challenge_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    points INT NOT NULL,
    challenge_type_id BIGINT UNSIGNED NOT NULL,
    map_piece_uri VARCHAR(2083),
    external_game_uri VARCHAR(2083),
    description TEXT,
    test_cases JSON,  -- Store test cases as JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (challenge_type_id) REFERENCES challenge_type(challenge_type_id)
);

CREATE TABLE challenge_code (
    challenge_code_id SERIAL PRIMARY KEY,
    challenge_id BIGINT UNSIGNED NOT NULL,
    language VARCHAR(50) NOT NULL,  -- Python, Java, C++, etc.
    code LONGTEXT NOT NULL,
    FOREIGN KEY (challenge_id) REFERENCES challenge(challenge_id) ON DELETE CASCADE
);

