-- :name get-sections-for-template* :result :*
-- :doc Return all sections for a template
SELECT id, templateId, name
FROM templateSections
WHERE templateId = :template-id;
