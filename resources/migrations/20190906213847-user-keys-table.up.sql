CREATE TABLE userLoginKeys
(id INTEGER PRIMARY KEY,
 userId INTEGER NOT NULL,
 value VARCHAR(200) NOT NULL,
 isValid BOOLEAN NOT NULL,
 createdAt TEXT NOT NULL);
