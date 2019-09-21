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
