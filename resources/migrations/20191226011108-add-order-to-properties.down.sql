ALTER TABLE templateSectionProperties RENAME TO _tmp1;
--;;
CREATE TABLE templateSectionProperties
(id INTEGER PRIMARY KEY,
templateSectionId INTEGER NOT NULL,
name TEXT NOT NULL,
isMultiple BOOLEAN NOT NULL,
valueType TEXT NOT NULL);
--;;
INSERT INTO templateSectionProperties (id, templateSectionId, name, isMultiple, valueType)
SELECT id, templateSectionId, name, isMultiple, valueType
FROM _tmp1;
--;;
DROP TABLE _tmp1;
