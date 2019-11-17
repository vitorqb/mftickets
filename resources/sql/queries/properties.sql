-- :name get-properties-for-template* :result :*
-- :doc Gets all properties for a template
SELECT
  properties.id,
  properties.templateSectionId,
  properties.name,
  properties.isMultiple,
  properties.valueType
FROM templateSectionProperties as properties
JOIN templateSections ON templateSections.id = properties.templateSectionId
WHERE templateSections.templateId = :template-id;

-- :name get-property* :result :1
-- :doc Gets a properties by id
SELECT
  properties.id,
  properties.templateSectionId,
  properties.name,
  properties.isMultiple,
  properties.valueType
FROM templateSectionProperties as properties
WHERE properties.id = :id;

-- :name get-properties-for-templates-ids* :result :*
-- :doc Gets all properties for a list of template ids.
SELECT
  properties.id,
  properties.templateSectionId,
  properties.name,
  properties.isMultiple,
  properties.valueType
FROM templateSectionProperties as properties
JOIN templateSections ON templateSections.id = properties.templateSectionId
WHERE templateSections.templateId IN (:v*:templates-ids);

-- :name delete-property!* :! :n
-- :doc Deletes a property
DELETE FROM templateSectionProperties WHERE id = :id;

-- :name update-property!* :! :n
-- :doc Updates a property
UPDATE templateSectionProperties
SET templateSectionId = :template-section-id,
    name = :name,
    isMultiple = :is-multiple,
    valueType = :value-type
WHERE id = :id;

-- :name create-property!* :insert :raw
-- :doc Creates a property
INSERT INTO templateSectionProperties (templateSectionId, name, isMultiple, valueType)
VALUES (:template-section-id, :name, :is-multiple, :value-type);
