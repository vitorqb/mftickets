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
