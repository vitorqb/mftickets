CREATE TABLE propertiesValues
(id INTEGER PRIMARY KEY,
 propertyId INTEGER NOT NULL,
 ticketId INTEGER NOT NULL,
 UNIQUE(propertyId, ticketId));
