
CREATE TABLE IF NOT EXISTS comunidades (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre varchar(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nickname varchar(255) NOT NULL,
    password varchar(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_comunidades (
    user_id INT,
    comunidad_id INT,
    PRIMARY KEY (user_id, comunidad_id),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (comunidad_id) REFERENCES comunidades(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS hilos (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    description TEXT,
    comunidad_id INT,
    owner_id INT,

    FOREIGN KEY (comunidad_id) REFERENCES comunidades(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS posts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    content TEXT,
    owner_id INT NOT NULL,
    parent_id INT,
    thread_id INT NOT NULL,

    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (thread_id) REFERENCES hilos(id) ON DELETE CASCADE
);