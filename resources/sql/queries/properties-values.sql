-- :name create-generic-property-value!* :insert :raw
-- :doc Creates the generic part of a property value
INSERT INTO propertiesValues (propertyId, ticketId) VALUES (:property-id, :ticket-id);

-- :name get-generic-property-value* :n :1
SELECT id, propertyId, ticketId
FROM propertiesValues
WHERE propertyId = :property-id AND ticketId = :ticket-id;
