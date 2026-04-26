
CREATE TABLE IF NOT EXISTS communities (
    id INT PRIMARY KEY AUTO_INCREMENT,
    community_name varchar(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email varchar(255) NOT NULL UNIQUE,
    nickname varchar(255) NOT NULL,
    password varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_communities (
    user_id INT,
    community_id INT,
    PRIMARY KEY (user_id, community_id),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS threads (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    description TEXT,
    community_id INT,
    owner_id INT,

    FOREIGN KEY (community_id) REFERENCES communities(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS favorite_threads (
    user_id INT,
    thread_id INT,
    PRIMARY KEY (user_id, thread_id),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS posts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    content TEXT,
    likes INT DEFAULT 0,
    dislikes INT DEFAULT 0,
    owner_id INT NOT NULL,
    parent_id INT,
    thread_id INT NOT NULL,

    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE
);