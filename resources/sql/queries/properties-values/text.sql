-- :name get-text-property-value* :result :1
-- :doc Get's a text property value
SELECT textPropertiesValues.value
FROM textPropertiesValues
where textPropertiesValues.propertyValueId = :id;

-- :name create-text-property-value!* :insert :raw
-- :doc Inserts a text property value in it's table
INSERT INTO textPropertiesValues (propertyValueId, value) VALUES (:id, :templates.properties.types.text/value);
