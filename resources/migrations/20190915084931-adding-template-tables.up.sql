CREATE TABLE templates
(id INTEGER PRIMARY KEY,
 name TEXT NOT NULL,
 projectId INTEGER NOT NULL,
 creationDate TEXT NOT NULL);
--;;
CREATE TABLE templateSections
(id INTEGER PRIMARY KEY,
 templateId INTEGER NOT NULL,
 name TEXT NOT NULL);
--;;
CREATE TABLE templateSectionProperties
(id INTEGER PRIMARY KEY,
 templateSectionId INTEGER NOT NULL,
 name TEXT NOT NULL,
 isMultiple BOOLEAN NOT NULL,
 valueType TEXT NOT NULL);
--;;
CREATE TABLE templateSectionPropertyTextValue
(id INTEGER PRIMARY KEY,
 templateSectionPropertyId INTEGER UNIQUE NOT NULL,
 length TEXT NOT NULL);
--;;
CREATE TABLE templateSectionPropertyRadioValue
(id INTEGER PRIMARY KEY,
 templateSectionPropertyId INTEGER UNIQUE NOT NULL);
--;;
CREATE TABLE templateSectionPropertyRadioValueOptions
(id INTEGER PRIMARY KEY,
 templateSectionPropertyRadioValueId INTEGER NOT NULL,
 value TEXT NOT NULL);
--;;
CREATE TABLE templateSectionPropertyDateValue
(id INTEGER PRIMARY KEY,
 templateSectionPropertyId INTEGER UNIQUE NOT NULL);
