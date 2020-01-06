-- :name get-date-property-value* :result :1
-- :doc Gets a date property value
SELECT datePropertiesValues.value
FROM datePropertiesValues
where datePropertiesValues.propertyValueId = :id;

-- :name create-date-property-value* :insert :raw
-- :doc Inserts the date property value
INSERT INTO datePropertiesValues (propertyValueId, value)
VALUES (:id, :templates.properties.types.date/value);
