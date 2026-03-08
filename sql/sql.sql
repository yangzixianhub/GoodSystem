create database message;
use message;
CREATE TABLE IF NOT EXISTS user
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    phoneNumber VARCHAR(15)  NOT NULL UNIQUE,
    createdTime DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS messages (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        content TEXT NOT NULL,
                                        userID INT,
                                        publishTime DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        isDeleted BOOLEAN DEFAULT FALSE,
                                        FOREIGN KEY (userID) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS replies (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       messageID INT NOT NULL,
                                       content TEXT NOT NULL,
                                       userID INT,
                                       replyTime DATETIME DEFAULT CURRENT_TIMESTAMP,
                                       isDeleted BOOLEAN DEFAULT FALSE,
                                       FOREIGN KEY (messageID) REFERENCES messages(id),
                                       FOREIGN KEY (userID) REFERENCES user(id)
    );