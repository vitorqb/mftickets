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
