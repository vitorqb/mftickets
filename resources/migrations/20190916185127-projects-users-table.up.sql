CREATE TABLE usersProjects
(id INTEGER PRIMARY KEY,
 userId INTEGER NOT NULL,
 projectId INTEGER NOT NULL,
 UNIQUE(userId, projectId));
