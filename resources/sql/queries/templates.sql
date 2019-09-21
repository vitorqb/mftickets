-- :name get-raw-template* :result :1
-- :doc Gets the raw values for a template
SELECT id, projectId, name, creationDate
FROM templates
WHERE id = :id;

-- :name get-raw-templates-for-project* :result :*
-- :doc Gets the raw values for templates of a project
SELECT id, projectId, name, creationDate
FROM templates
WHERE projectId = :project-id;
