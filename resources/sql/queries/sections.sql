-- :name get-sections-for-template* :result :*
-- :doc Return all sections for a template
SELECT id, templateId, name
FROM templateSections
WHERE templateId = :template-id;

-- :name get-sections-for-templates-ids* :result :*
-- :doc Return all sections for a list of template ids.
SELECT id, templateId, name
FROM templateSections
WHERE templateId IN (:v*:templates-ids);
