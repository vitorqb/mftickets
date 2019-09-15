-- :name get-raw-template* :result :1
-- :doc Gets the raw values for a template
SELECT id, projectId, name, creationDate
FROM templates
WHERE id = :id;
