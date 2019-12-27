ALTER TABLE templateSections RENAME TO _tmp1;
--;;
CREATE TABLE templateSections
(id INTEGER PRIMARY KEY,
templateId INTEGER NOT NULL,
name TEXT NOT NULL);
--;;
INSERT INTO templateSections (id, templateId, name) SELECT id, templateId, name FROM _tmp1;
--;;
DROP TABLE _tmp1;
