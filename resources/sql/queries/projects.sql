-- :name get-project* :result :1
-- :doc Get's a project by it's id
SELECT id, name, description
FROM projects
WHERE id = :id;
