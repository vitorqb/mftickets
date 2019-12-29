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
/*~ (when (:name-like params) */
AND name LIKE :value:name-like
/*~ ) ~*/
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
WHERE projectId = :project-id
/*~ (when (:name-like params) */
AND name LIKE :value:name-like
/*~ ) ~*/
;

-- :name update-raw-template!* :! :n
-- :doc Updates the value for a template
UPDATE templates
SET projectId = :project-id, name = :name
WHERE id = :id;

-- :name create-template!* :insert :raw
-- :doc Creates a template.
INSERT INTO templates (name, projectId, creationDate)
VALUES (:name, :project-id, :creation-date);

-- :name unique-template-name-for-project?* :result :1
-- :doc Returns true or false in response.
SELECT EXISTS(SELECT 1 FROM templates WHERE name = :name AND projectId = :project-id) AS response;

-- :name delete-template* :! :n
-- :doc Deletes a template
DELETE FROM templates WHERE id = :id;
