-- :name get-user* :? :1
-- :doc Gets and returns an user
SELECT id, email
FROM users
WHERE email = :email

-- :name get-user-by-id* :? :1
-- :doc Gets and returns an user
SELECT id, email
FROM users
WHERE id = :id

-- :name create-user!* :insert
-- :doc Creates an user
INSERT INTO users (email)
VALUES (:email)

-- :name get-projects-ids-for-user* :result :*
-- :doc Returns all project ids for an user
SELECT projectId FROM usersProjects WHERE userId = :id
