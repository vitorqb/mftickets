-- :name get-project* :result :1
-- :doc Get's a project by it's id
SELECT id, name, description
FROM projects
WHERE id = :id;

-- :name get-projects-for-user* :result :*
-- :doc Get's all projects for an user.
SELECT projects.id, projects.name, projects.description
FROM usersProjects
JOIN users ON usersProjects.userId = users.id
JOIN projects ON usersProjects.projectId = projects.id
WHERE users.id = :user-id;

-- :name create-project!* :insert :raw
-- :doc Creates a project.
INSERT INTO projects (name, description)
VALUES (:name, :description);

-- :name create-user-project!* :insert :raw
-- :doc Creates an entry on the usersProjects table.
INSERT INTO usersProjects (userId, projectId)
VALUES (:user-id, :project-id);

-- :name update-project!* :! :n
-- :doc Updates a project
UPDATE projects
SET name = :name, description = :description
WHERE id = :id;

-- :name delete-project!* :! :n
-- :doc Deletes a project
DELETE FROM projects WHERE id = :id;

-- :name delete-all-users-for-project!* :! :n
-- :doc Deletes all users from usersProjects for a given project
DELETE FROM usersProjects WHERE projectId = :id;

-- :name user-has-access-to-template?* :result :1
-- :doc Returns true if a user-id has access to template-id
SELECT 1 as response
FROM usersProjects
JOIN projects ON projects.id = usersProjects.projectId
JOIN templates ON templates.projectId = projects.id
WHERE templates.id = :template-id AND usersProjects.userId = :user-id;
      
