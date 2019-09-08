-- :name invalidate-user-keys! :! :n
-- :doc Invalid all keys for an user.
UPDATE userLoginKeys
SET isValid = FALSE
WHERE userId = :value:user-id;

-- :name create-user-key!* :insert :raw
-- :doc Creates an user key.
INSERT INTO userLoginKeys (userId, value, isValid, createdAt)
VALUES (:user-id, :value, :is-valid?, :created-at);

-- :name get-user-key* :result :1
-- :doc Retrieves an user key.
SELECT id, userId, value, isValid, createdAt
FROM userLoginKeys
WHERE id = :id
