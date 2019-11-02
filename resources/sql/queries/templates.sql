-- :name get-raw-template* :result :1
-- :doc Gets the raw values for a template
SELECT id, projectId, name, creationDate
FROM templates
WHERE id = :id;

-- :name get-raw-templates-for-project* :result :*
-- :doc Gets the raw values for templates of a project
SELECT id, projectId, name, creationDate
FROM templates
WHERE projectId = :project-id
/*~ (when (:pagination params) */
ORDER BY id ASC
LIMIT :value:pagination.limit
OFFSET :value:pagination.offset
/*~ ) ~*/
;

-- :name count-templates-for-project* :result :1
-- :doc Counts how many templates there are for a project.
SELECT COUNT(*) AS response
FROM templates
WHERE projectId = :project-id;
