-- :snip select-snip
SELECT
  properties.id,
  properties.templateSectionId,
  properties.name,
  properties.isMultiple,
  properties.valueType,
  properties.orderIndex
FROM templateSectionProperties as properties

-- :name get-generic-properties-for-template* :result :*
-- :doc Gets all properties for a template
:snip:select
JOIN templateSections ON templateSections.id = properties.templateSectionId
WHERE templateSections.templateId = :template-id;

-- :name get-generic-properties-for-section* :result :*
-- :doc Gets all properties for a section
:snip:select
WHERE properties.templateSectionId = :section-id;

-- :name get-generic-property* :result :1
-- :doc Gets a properties by id
:snip:select
WHERE properties.id = :id
LIMIT 1;

-- :name get-generic-properties-for-templates-ids* :result :*
-- :doc Gets all properties for a list of template ids.
:snip:select
JOIN templateSections ON templateSections.id = properties.templateSectionId
WHERE templateSections.templateId IN (:v*:templates-ids);

-- :name delete-property!* :! :n
-- :doc Deletes a property
DELETE FROM templateSectionProperties WHERE id = :id;

-- :name update-property-generic-data!* :! :n
-- :doc Updates a property
UPDATE templateSectionProperties
SET templateSectionId = :template-section-id,
    name = :name,
    isMultiple = :is-multiple,
    valueType = :value-type,
    orderIndex = :order
WHERE id = :id;

-- :name create-generic-property!* :insert :raw
-- :doc Creates a property
INSERT INTO templateSectionProperties (templateSectionId, name, isMultiple, valueType, orderIndex)
VALUES (:template-section-id, :name, :is-multiple, :value-type, :order);

-- :name create-radio-option!* :insert :raw
-- :doc Inserts a single radio question in the db
INSERT INTO templatePropertiesRadioOptions (propertyId, value)
VALUES (:property-id, :value);

-- :name get-radio-options* :result :*
-- :doc Get all radio options for a given property
SELECT id, propertyId, value FROM templatePropertiesRadioOptions WHERE propertyId = :property-id;

-- :name delete-all-radio-options-for-property!* :! :n
-- :doc Deletes all radio options for a given property-id
DELETE FROM templatePropertiesRadioOptions WHERE propertyId = :property-id;
