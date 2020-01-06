-- :snip select-snip
SELECT
  properties.id,
  properties.templateSectionId,
  properties.name,
  properties.isMultiple,
  properties.valueType,
  properties.orderIndex
FROM templateSectionProperties as properties

-- :name get-properties-for-template* :result :*
-- :doc Gets all properties for a template
:snip:select
JOIN templateSections ON templateSections.id = properties.templateSectionId
WHERE templateSections.templateId = :template-id;

-- :name get-properties-for-ticket* :result :*
-- :doc Gets all properties for a ticket
:snip:select
JOIN templateSections ON templateSections.id = properties.templateSectionId
JOIN templates ON templates.id = templateSections.templateId
JOIN tickets ON templates.id = tickets.templateId
WHERE tickets.id = :ticket-id;

-- :name get-properties-for-section* :result :*
-- :doc Gets all properties for a section
:snip:select
WHERE properties.templateSectionId = :section-id;

-- :name get-property* :result :1
-- :doc Gets a properties by id
:snip:select
WHERE properties.id = :id
LIMIT 1;

-- :name get-properties-for-templates-ids* :result :*
-- :doc Gets all properties for a list of template ids.
:snip:select
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
    valueType = :value-type,
    orderIndex = :order
WHERE id = :id;

-- :name create-property!* :insert :raw
-- :doc Creates a property
INSERT INTO templateSectionProperties (templateSectionId, name, isMultiple, valueType, orderIndex)
VALUES (:template-section-id, :name, :is-multiple, :value-type, :order);
